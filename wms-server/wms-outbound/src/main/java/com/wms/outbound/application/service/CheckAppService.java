package com.wms.outbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.common.util.DocNoUtil;
import com.wms.outbound.application.assembler.CheckAssembler;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.CheckRepository;
import com.wms.outbound.domain.repository.OrderRepository;
import com.wms.outbound.domain.repository.WaveRepository;
import com.wms.outbound.infrastructure.mapper.CheckHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckAppService {
    private final CheckRepository checkRepository;
    private final CheckHeaderMapper checkMapper;
    private final WaveRepository waveRepository;
    private final OrderRepository orderRepository;
    private final CheckAssembler assembler;

    public IPage<CheckDTO> pageChecks(CheckPageQuery query) {
        IPage<CheckHeader> result =
                checkMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<CheckHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        CheckHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        CheckHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(CheckHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public CheckDTO getCheckDetail(Long id) {
        CheckHeader h =
                checkRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("复核单不存在"));
        CheckDTO dto = assembler.toDTO(h);
        dto.setLines(checkRepository.findLines(id).stream().map(assembler::toLineDTO).toList());
        return dto;
    }

    /** 按波次生成复核单；已存在时直接复用，保证 PDA 重复进入任务不会重复建单。 */
    @Transactional
    public CheckResultDTO createCheckForWave(Long waveHeaderId) {
        Optional<CheckHeader> existing = checkRepository.findByWave(waveHeaderId);
        if (existing.isPresent()) {
            CheckHeader h = existing.get();
            return new CheckResultDTO(h.getCheckNo(), h.getId());
        }

        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        WaveHeader wave =
                waveRepository
                        .findById(waveHeaderId)
                        .orElseThrow(() -> BusinessException.notFound("波次不存在"));

        String checkNo = DocNoUtil.next("CHK");
        CheckHeader h = new CheckHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(wave.getWarehouseId());
        h.setCheckNo(checkNo);
        h.setWaveHeaderId(waveHeaderId);
        h.setStatus(CheckHeader.Status.CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        checkRepository.saveHeader(h);

        int lineNo = 0;
        for (WaveLine wl : waveRepository.findLines(waveHeaderId)) {
            for (OrderLine ol : orderRepository.findLines(wl.getOrderHeaderId())) {
                lineNo++;
                CheckLine cl = new CheckLine();
                cl.setTenantId(tenantId);
                cl.setCheckHeaderId(h.getId());
                cl.setLineNo(lineNo);
                cl.setOrderHeaderId(ol.getOrderHeaderId());
                cl.setSkuId(ol.getSkuId());
                cl.setSkuCode(ol.getSkuCode());
                cl.setSkuName(ol.getSkuName());
                cl.setOrderQty(ol.getOrderQty());
                cl.setCheckQty(BigDecimal.ZERO);
                // null 表示尚未复核，与 PDA 端 isMatch == null 的判定保持一致
                cl.setIsMatch(null);
                cl.setCreatedBy(userId);
                cl.setUpdatedBy(userId);
                checkRepository.saveLine(cl);
            }
        }
        return new CheckResultDTO(checkNo, h.getId());
    }

    @Transactional
    public void submitCheck(CheckSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        CheckHeader h =
                checkRepository
                        .findById(cmd.getCheckHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("复核单不存在"));
        if (h.getStatus() == CheckHeader.Status.PASS || h.getStatus() == CheckHeader.Status.DONE) {
            throw BusinessException.conflict("复核单已完成，不可重复复核");
        }
        // REJECT（存在差异）允许继续提交：复核员修正后可重新判定

        List<CheckLine> lines = checkRepository.findLines(h.getId());
        CheckLine line =
                lines.stream()
                        .filter(l -> l.getId().equals(cmd.getCheckLineId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("复核行不存在"));

        line.setCheckQty(cmd.getCheckQty());
        line.setIsMatch(cmd.getIsMatch());
        line.setDiffReason(cmd.getDiffReason());
        line.setFromContainer(cmd.getFromContainer());
        line.setUpdatedBy(userId);
        checkRepository.updateLine(line);

        // 首次确认进入复核中
        if (h.getStatus() == CheckHeader.Status.CREATED) {
            h.setStatus(CheckHeader.Status.CHECKING);
            h.setCheckBy(userId);
            h.setStartTime(LocalDateTime.now());
        }

        // 全部行复核完成 → 无差异 PASS，有差异 REJECT 待人工处理
        boolean allChecked = lines.stream().allMatch(l -> l.getIsMatch() != null);
        if (allChecked) {
            boolean hasDiff =
                    lines.stream().anyMatch(l -> Integer.valueOf(0).equals(l.getIsMatch()));
            h.setStatus(hasDiff ? CheckHeader.Status.REJECT : CheckHeader.Status.PASS);
            h.setEndTime(LocalDateTime.now());
        }
        h.setUpdatedBy(userId);
        checkRepository.updateHeader(h);
    }
}
