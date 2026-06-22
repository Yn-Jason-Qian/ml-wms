package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuUpdateCmd {
    @NotNull private Long id;
    @NotBlank @Size(max = 64) private String skuCode;
    @NotBlank @Size(max = 256) private String skuName;
    private String skuDesc;
    private String barcode;
    private String category;
    private String brand;
    private String spec;
    private Long baseUnitId;
    private BigDecimal baseLength;
    private BigDecimal baseWidth;
    private BigDecimal baseHeight;
    private BigDecimal baseWeight;
    private BigDecimal baseVolume;
    private Integer shelfLife;
    private String shelfLifeType;
    private Integer batchManaged;
    private Integer snManaged;
    private String lotAttrs;
}
