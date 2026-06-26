package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Unit.UnitType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnitDTO {
    private Long id;
    private String unitCode;
    private String unitName;
    private UnitType unitType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
