package com.wms.masterdata.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_warehouse")
public class Warehouse extends BaseEntity {

    private String whCode;
    private String whName;
    private String whType;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer status;

    /** 是否启用 */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /** 禁用仓库前校验：无库存时才能禁用 */
    public void disable() {
        this.status = 0;
    }

    public void enable() {
        this.status = 1;
    }

    /** 仓库编码规则校验 */
    public void validateCode() {
        if (whCode == null || !whCode.matches("^WH-[A-Z0-9]{2,16}$")) {
            throw new IllegalArgumentException("仓库编码格式错误，需为 WH- 前缀 + 2~16位大写字母/数字");
        }
    }
}
