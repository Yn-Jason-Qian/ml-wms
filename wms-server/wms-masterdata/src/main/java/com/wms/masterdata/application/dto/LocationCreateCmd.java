package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long areaId;

    @NotBlank
    @Size(max = 64)
    private String locationCode;

    @Size(max = 128)
    private String locationName;

    @NotBlank private String locationType;
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
}
