package com.wms.masterdata.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_area")
public class Area extends BaseEntity {

    /** 库区类型 */
    public enum AreaType {
        RECEIVE,
        SHIPPING,
        STORAGE,
        PICKING,
        RETURN,
        QC
    }

    private Long warehouseId;
    private String areaCode;
    private String areaName;
    private AreaType areaType;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private Integer status;

    public void validateCode() {
        if (areaCode == null || !areaCode.matches("^AR-[A-Z0-9]{2,16}$")) {
            throw new IllegalArgumentException("库区编码格式错误，需为 AR- 前缀 + 2~16位大写字母/数字");
        }
    }

    public void disable() {
        this.status = 0;
    }

    public void enable() {
        this.status = 1;
    }

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
