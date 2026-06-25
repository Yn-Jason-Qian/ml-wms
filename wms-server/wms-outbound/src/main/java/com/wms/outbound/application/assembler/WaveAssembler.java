package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.WaveDTO;
import com.wms.outbound.domain.entity.WaveHeader;

import org.springframework.stereotype.Component;

@Component
public class WaveAssembler {

    public WaveDTO toDTO(WaveHeader h) {
        WaveDTO d = new WaveDTO();
        d.setId(h.getId());
        d.setWaveNo(h.getWaveNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setWaveType(h.getWaveType());
        d.setWaveStatus(h.getWaveStatus());
        d.setOrderCount(h.getOrderCount());
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }
}
