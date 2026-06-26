package com.wms.outbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.outbound.application.assembler.PickAssembler;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.gateway.InventoryGateway;
import com.wms.outbound.domain.repository.OrderRepository;
import com.wms.outbound.domain.repository.PickRepository;
import com.wms.outbound.domain.repository.WaveRepository;
import com.wms.outbound.infrastructure.mapper.PickHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PickAppService {
    private final PickRepository pickRepository;
    private final PickHeaderMapper pickMapper;
    private final WaveRepository waveRepository;
    private final OrderRepository orderRepository;
    private final InventoryGateway inventoryGateway;
    private final PickAssembler assembler;

    public IPage<PickDTO> pagePicks(PickPageQuery query) {
        IPage<PickHeader> result =
                pickMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<PickHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        PickHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        PickHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(PickHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    public PickDTO getPickDetail(Long id) {
        PickHeader h =
                pickRepository.findById(id).orElseThrow(() -> BusinessException.notFound("拣货单不存在"));
        PickDTO dto = assembler.toDTO(h);
        dto.setLines(pickRepository.findLines(id).stream().map(assembler::toLineDTO).toList());
        return dto;
    }

    @Transactional
    public PickResultDTO createPickForWave(Long waveHeaderId) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String pickNo =
                "PICK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        WaveHeader wave =
                waveRepository
                        .findById(waveHeaderId)
                        .orElseThrow(() -> BusinessException.notFound("波次不存在"));

        PickHeader h = new PickHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(wave.getWarehouseId());
        h.setPickNo(pickNo);
        h.setWaveHeaderId(waveHeaderId);
        h.setPickType(PickHeader.PickType.RF);
        h.setStatus(PickHeader.Status.CREATED);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        pickRepository.saveHeader(h);

        int lineNo = 0;
        for (WaveLine wl : waveRepository.findLines(waveHeaderId)) {
            for (OrderLine ol : orderRepository.findLines(wl.getOrderHeaderId())) {
                lineNo++;
                PickLine pl = new PickLine();
                pl.setTenantId(tenantId);
                pl.setPickHeaderId(h.getId());
                pl.setLineNo(lineNo);
                pl.setOrderHeaderId(ol.getOrderHeaderId());
                pl.setOrderLineId(ol.getId());
                pl.setSkuId(ol.getSkuId());
                pl.setSkuCode(ol.getSkuCode());
                pl.setSkuName(ol.getSkuName());
                pl.setPickQty(ol.getOrderQty());
                pl.setPickedQty(BigDecimal.ZERO);
                Long locId =
                        inventoryGateway.findLocationBySku(
                                tenantId, ol.getSkuId(), ol.getBatchNo());
                pl.setLocationId(locId != null ? locId : 0L);
                pl.setBatchNo(ol.getBatchNo());
                pl.setLotAttrs(ol.getLotAttrs());
                pl.setStatus("CREATED");
                pl.setCreatedBy(userId);
                pl.setUpdatedBy(userId);
                pickRepository.saveLine(pl);
            }
        }
        return new PickResultDTO(pickNo, h.getId());
    }

    @Transactional
    public void submitPick(PickSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        PickHeader h =
                pickRepository
                        .findById(cmd.getPickHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("拣货单不存在"));
        PickLine l =
                pickRepository.findLines(h.getId()).stream()
                        .filter(p -> p.getId().equals(cmd.getPickLineId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("拣货行不存在"));

        l.setPickedQty(cmd.getPickedQty());
        l.setToContainer(cmd.getToContainer());
        l.setStatus("PICKED");
        l.setPickBy(userId);
        l.setPickAt(LocalDateTime.now());
        l.setUpdatedBy(userId);
        pickRepository.updateLine(l);

        boolean allDone =
                pickRepository.findLines(h.getId()).stream()
                        .allMatch(p -> "PICKED".equals(p.getStatus()));
        if (allDone) {
            h.setStatus(PickHeader.Status.PICKED);
            pickRepository.updateHeader(h);
        }
    }
}
