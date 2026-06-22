package com.wms.masterdata.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_sku")
public class Sku extends BaseEntity {

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
    private String shelfLifeType;
    private Integer batchManaged;
    private Integer snManaged;
    private String lotAttrs;
    private Integer status;

    public static final String SHELF_LIFE_PRODUCTION = "PRODUCTION";
    public static final String SHELF_LIFE_RECEIVE = "RECEIVE";

    public void disable() { this.status = 0; }
    public void enable() { this.status = 1; }
    public boolean isEnabled() { return status != null && status == 1; }
    public boolean checkBatchManaged() { return batchManaged != null && batchManaged == 1; }
    public boolean checkSnManaged() { return snManaged != null && snManaged == 1; }
}
