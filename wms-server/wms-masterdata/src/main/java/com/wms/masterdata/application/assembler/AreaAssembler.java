package com.wms.masterdata.application.assembler;

import com.wms.masterdata.application.dto.AreaCreateCmd;
import com.wms.masterdata.application.dto.AreaDTO;
import com.wms.masterdata.application.dto.AreaUpdateCmd;
import com.wms.masterdata.domain.entity.Area;
import org.springframework.stereotype.Component;

@Component
public class AreaAssembler {
    public Area toEntity(AreaCreateCmd cmd) {
        Area a = new Area();
        a.setWarehouseId(cmd.getWarehouseId());
        a.setAreaCode(cmd.getAreaCode());
        a.setAreaName(cmd.getAreaName());
        a.setAreaType(cmd.getAreaType());
        a.setTemperatureMin(cmd.getTemperatureMin());
        a.setTemperatureMax(cmd.getTemperatureMax());
        a.setStatus(1);
        return a;
    }

    public void mergeToEntity(AreaUpdateCmd cmd, Area area) {
        area.setAreaCode(cmd.getAreaCode());
        area.setAreaName(cmd.getAreaName());
        area.setAreaType(cmd.getAreaType());
        area.setTemperatureMin(cmd.getTemperatureMin());
        area.setTemperatureMax(cmd.getTemperatureMax());
    }

    public AreaDTO toDTO(Area area) {
        AreaDTO dto = new AreaDTO();
        dto.setId(area.getId());
        dto.setWarehouseId(area.getWarehouseId());
        dto.setAreaCode(area.getAreaCode());
        dto.setAreaName(area.getAreaName());
        dto.setAreaType(area.getAreaType());
        dto.setTemperatureMin(area.getTemperatureMin());
        dto.setTemperatureMax(area.getTemperatureMax());
        dto.setStatus(area.getStatus());
        dto.setCreatedAt(area.getCreatedAt());
        dto.setUpdatedAt(area.getUpdatedAt());
        return dto;
    }
}
