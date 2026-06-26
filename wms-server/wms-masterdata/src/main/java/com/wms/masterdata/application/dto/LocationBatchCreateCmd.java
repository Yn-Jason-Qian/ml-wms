package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 批量生成库位命令 —— 按规则自动生成库位编码。 例：warehouseId=1, areaId=2, warehousePrefix="WH01", aisleRange=1~3,
 * shelfRange=1~5, tierRange=1~4, depthRange=1~2 生成 3×5×4×2=120 个库位，编码如 WH01-01-01-01-01
 */
@Data
public class LocationBatchCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long areaId;
    @NotBlank private String warehousePrefix;
    @NotNull private com.wms.masterdata.domain.entity.Location.LocationType locationType;
    @NotNull private Integer aisleFrom;
    @NotNull private Integer aisleTo;
    @NotNull private Integer shelfFrom;
    @NotNull private Integer shelfTo;
    @NotNull private Integer tierFrom;
    @NotNull private Integer tierTo;
    @NotNull private Integer depthFrom;
    @NotNull private Integer depthTo;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private Integer maxQty;
}
