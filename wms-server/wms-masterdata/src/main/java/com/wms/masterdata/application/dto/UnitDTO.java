package com.wms.masterdata.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnitDTO {
    private Long id;
    private String unitCode;
    private String unitName;
    private String unitType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
