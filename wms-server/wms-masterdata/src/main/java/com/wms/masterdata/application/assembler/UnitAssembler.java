package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.UnitDTO;
import com.wms.masterdata.domain.entity.Unit;

import org.springframework.stereotype.Component;

@Component
public class UnitAssembler {
    public UnitDTO toDTO(Unit u) {
        UnitDTO dto = new UnitDTO();
        dto.setId(u.getId());
        dto.setUnitCode(u.getUnitCode());
        dto.setUnitName(u.getUnitName());
        dto.setUnitType(u.getUnitType());
        dto.setStatus(u.getStatus());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        return dto;
    }
}
