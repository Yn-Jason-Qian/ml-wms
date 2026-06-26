package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.SkuPackage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuPackageCreateCmd {
    private Long skuId;
    @NotNull private SkuPackage.PackageLevel packageLevel;
    @NotBlank private String packageName;
    @NotNull private Long unitId;
    @NotNull private BigDecimal qtyPerParent;
    private String barcode;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer isDefaultReceive;
    private Integer isDefaultPick;
    private Integer isDefaultStorage;
}
