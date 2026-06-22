package com.wms.masterdata.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AreaDTO {
    private Long id;
    private Long warehouseId;
    private String areaCode;
    private String areaName;
    private String areaType;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
