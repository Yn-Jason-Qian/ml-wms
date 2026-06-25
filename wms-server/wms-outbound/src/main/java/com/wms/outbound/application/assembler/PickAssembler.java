package com.wms.outbound.application.assembler;

import com.wms.outbound.application.dto.PickDTO;
import com.wms.outbound.domain.entity.PickHeader;

import org.springframework.stereotype.Component;

@Component
public class PickAssembler {

    public PickDTO toDTO(PickHeader h) {
        PickDTO d = new PickDTO();
        d.setId(h.getId());
        d.setPickNo(h.getPickNo());
        d.setWarehouseId(h.getWarehouseId());
        d.setPickType(h.getPickType());
        d.setStatus(h.getStatus());
        d.setAssignTo(h.getAssignTo() != null ? h.getAssignTo() : 0L);
        d.setCreatedAt(h.getCreatedAt());
        return d;
    }
}
