package com.wms.outbound.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.outbound.application.assembler.ShipAssembler;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.PickRepository;
import com.wms.outbound.domain.repository.ShipRepository;
import com.wms.outbound.domain.repository.WaveRepository;
import com.wms.outbound.domain.service.ShipDomainService;
import com.wms.outbound.infrastructure.mapper.ShipHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ShipAppService {
    private final ShipRepository shipRepository;
    private final ShipHeaderMapper shipMapper;
    private final WaveRepository waveRepository;
    private final PickRepository pickRepository;
    private final ShipDomainService shipDomainService;
    private final ShipAssembler assembler;

    public IPage<ShipDTO> pageShips(ShipPageQuery query) {
        IPage<ShipHeader> result =
                shipMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<ShipHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        ShipHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        ShipHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(ShipHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public ShipResultDTO createShip(ShipCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String shipNo =
                "SHP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        ShipHeader h = new ShipHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setOwnerId(cmd.getOwnerId());
        h.setShipNo(shipNo);
        h.setWaveHeaderId(cmd.getWaveHeaderId());
        h.setDeliveryMethod(cmd.getDeliveryMethod());
        h.setCarrierName(cmd.getCarrierName());
        h.setTrackingNo(cmd.getTrackingNo());
        h.setPackageCount(cmd.getPackageCount());
        h.setGrossWeight(cmd.getGrossWeight());
        h.setVolume(cmd.getVolume());
        h.setStatus(ShipHeader.Status.DONE);
        h.setShipBy(userId);
        h.setShipAt(LocalDateTime.now());
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        shipRepository.saveHeader(h);

        WaveHeader wave = waveRepository.findById(cmd.getWaveHeaderId()).orElse(null);
        if (wave != null) {
            for (WaveLine wl : waveRepository.findLines(wave.getId())) {
                PickHeader pickH = pickRepository.findByWave(wave.getId()).orElse(null);
                if (pickH != null) {
                    for (PickLine pl : pickRepository.findLines(pickH.getId())) {
                        if ("PICKED".equals(pl.getStatus())) {
                            shipDomainService.shipDeduct(pl, userId);
                        }
                    }
                }
            }
        }
        return new ShipResultDTO(shipNo, h.getId());
    }
}
