package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Sku.ShelfLifeType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SkuDTO {
    private Long id;
    private Long ownerId;
    private String skuCode;
    private String skuName;
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
    private ShelfLifeType shelfLifeType;
    private Integer batchManaged;
    private Integer snManaged;
    private String lotAttrs;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 包装规格列表（查询详情时附带） */
    private List<SkuPackageDTO> packages;
}
