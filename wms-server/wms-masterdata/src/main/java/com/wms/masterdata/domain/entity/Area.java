package com.wms.masterdata.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_area")
public class Area extends BaseEntity {

    private Long warehouseId;
    private String areaCode;
    private String areaName;
    private String areaType;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private Integer status;

    /** 库区类型枚举常量 */
    public static final String TYPE_RECEIVE = "RECEIVE";
    public static final String TYPE_SHIPPING = "SHIPPING";
    public static final String TYPE_STORAGE = "STORAGE";
    public static final String TYPE_PICKING = "PICKING";
    public static final String TYPE_RETURN = "RETURN";
    public static final String TYPE_QC = "QC";

    public void validateCode() {
        if (areaCode == null || !areaCode.matches("^AR-[A-Z0-9]{2,16}$")) {
            throw new IllegalArgumentException("库区编码格式错误，需为 AR- 前缀 + 2~16位大写字母/数字");
        }
    }

    public void disable() { this.status = 0; }
    public void enable() { this.status = 1; }
    public boolean isEnabled() { return status != null && status == 1; }
}
