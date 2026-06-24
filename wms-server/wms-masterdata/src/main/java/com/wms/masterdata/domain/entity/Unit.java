package com.wms.masterdata.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_unit")
public class Unit extends BaseEntity {

    private String unitCode;
    private String unitName;
    private String unitType;
    private Integer status;

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
