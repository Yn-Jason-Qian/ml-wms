package com.wms.masterdata.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_sku_package")
public class SkuPackage extends BaseEntity {

    /** 包装层级 */
    public enum PackageLevel {
        EA,
        CASE,
        PALLET
    }

    private Long skuId;
    private PackageLevel packageLevel;
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

    /** 校验包装层级不能重复 */
    public void validateUnique(SkuPackage existing) {
        if (existing != null && !existing.getId().equals(this.getId())) {
            throw new IllegalArgumentException("该包装层级 [" + packageLevel + "] 已存在");
        }
    }
}
