package com.wms.inbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inbound.application.assembler.PutawayAssembler;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.domain.entity.PutawayHeader;
import com.wms.inbound.domain.entity.PutawayLine;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;
import com.wms.inbound.domain.repository.PutawayRepository;
import com.wms.inbound.domain.repository.ReceiveRepository;
import com.wms.inbound.domain.service.PutawayDomainService;
import com.wms.inbound.infrastructure.mapper.PutawayHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutawayAppService {
    private final PutawayRepository putawayRepository;
    private final PutawayHeaderMapper putawayHeaderMapper;
    private final PutawayAssembler assembler;
    private final PutawayDomainService putawayDomainService;
    private final ReceiveRepository receiveRepository;

    public IPage<PutawayDTO> pagePutaways(PutawayPageQuery query) {
        IPage<PutawayHeader> result =
                putawayHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<PutawayHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        PutawayHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        PutawayHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(PutawayHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public PutawayDTO getPutaway(Long id) {
        PutawayHeader h =
                putawayRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("上架单不存在"));
        PutawayDTO dto = assembler.toDTO(h);
        if (h.getReceiveHeaderId() != null) {
            receiveRepository
                    .findById(h.getReceiveHeaderId())
                    .ifPresent(
                            rcv -> {
                                dto.setReceiveNo(rcv.getReceiveNo());
                                dto.setOwnerId(rcv.getOwnerId());
                            });
        }
        if (dto.getReceiveNo() == null) {
            dto.setReceiveNo("");
        }
        dto.setLines(
                putawayRepository.findLinesByHeader(id).stream()
                        .map(assembler::toLineDTO)
                        .toList());
        return dto;
    }

    @Transactional
    public PutawayResultDTO createPutaway(PutawayCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String paNo =
                "PA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        ReceiveHeader rcv =
                receiveRepository
                        .findById(cmd.getReceiveHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("收货单不存在"));
        List<ReceiveLine> rcvLines = receiveRepository.findLinesByHeader(rcv.getId());

        PutawayHeader h = new PutawayHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setPutawayNo(paNo);
        h.setReceiveHeaderId(cmd.getReceiveHeaderId());
        h.setStatus(PutawayHeader.Status.CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        putawayRepository.saveHeader(h);

        int lineNo = 0;
        for (ReceiveLine rl : rcvLines) {
            lineNo++;
            PutawayLine pl = new PutawayLine();
            pl.setTenantId(tenantId);
            pl.setPutawayHeaderId(h.getId());
            pl.setLineNo(lineNo);
            pl.setSkuId(rl.getSkuId());
            pl.setSkuCode(rl.getSkuCode());
            pl.setSkuName(rl.getSkuName());
            pl.setPutawayQty(rl.getReceiveQty());
            pl.setDoneQty(BigDecimal.ZERO);
            pl.setFromLocationId(rl.getReceiveLocationId());
            pl.setBatchNo(rl.getBatchNo());
            pl.setLotAttrs(rl.getLotAttrs());
            pl.setStatus("CREATED");
            pl.setToLocationId(0L);
            pl.setCreatedBy(userId);
            pl.setUpdatedBy(userId);
            putawayRepository.saveLine(pl);
        }

        h.setStatus(PutawayHeader.Status.PUTAWAYING);
        putawayRepository.updateHeader(h);
        return new PutawayResultDTO(paNo, h.getId());
    }

    @Transactional
    public void submitPutaway(PutawaySubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        PutawayHeader paHeader =
                putawayRepository
                        .findById(cmd.getPutawayHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("上架单不存在"));
        PutawayLine line =
                putawayRepository.findLinesByHeader(paHeader.getId()).stream()
                        .filter(l -> l.getId().equals(cmd.getPutawayLineId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("上架行不存在"));

        if (cmd.getToLocationId() != null) {
            line.setToLocationId(cmd.getToLocationId());
        }

        Long warehouseId = paHeader.getWarehouseId();
        Long ownerId =
                receiveRepository
                        .findById(paHeader.getReceiveHeaderId())
                        .map(ReceiveHeader::getOwnerId)
                        .orElse(0L);

        putawayDomainService.executePutaway(line, warehouseId, ownerId, userId);

        line.setDoneQty(line.getPutawayQty());
        line.setStatus("DONE");
        line.setPutawayBy(userId);
        line.setPutawayAt(LocalDateTime.now());
        line.setUpdatedBy(userId);
        putawayRepository.updateLine(line);

        List<PutawayLine> lines = putawayRepository.findLinesByHeader(cmd.getPutawayHeaderId());
        boolean allDone = lines.stream().allMatch(l -> "DONE".equals(l.getStatus()));
        if (allDone) {
            PutawayHeader h = putawayRepository.findById(cmd.getPutawayHeaderId()).orElse(null);
            if (h != null) {
                h.setStatus(PutawayHeader.Status.DONE);
                putawayRepository.updateHeader(h);
            }
        }
    }
}
