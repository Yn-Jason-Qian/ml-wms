package com.wms.masterdata.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuPackageDTO {
    private Long id;
    private Long skuId;
    private String packageLevel;
    private String packageName;
    private Long unitId;
    private BigDecimal qtyPerParent;
    private String barcode;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer isDefaultReceive;
    private Integer isDefaultPick;
    private Integer isDefaultStorage;
}
