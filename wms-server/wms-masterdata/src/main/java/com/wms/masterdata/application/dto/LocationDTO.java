package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Location.LocationType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private Long id;
    private Long warehouseId;
    private Long areaId;
    private String locationCode;
    private String locationName;
    private LocationType locationType;
    private String aisle;
    private String shelf;
    private String tier;
    private String depthPos;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private Integer maxQty;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String roadway;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
