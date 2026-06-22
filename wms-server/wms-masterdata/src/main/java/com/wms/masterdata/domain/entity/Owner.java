package com.wms.masterdata.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_owner")
public class Owner extends BaseEntity {

    private String ownerCode;
    private String ownerName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;

    public void validateCode() {
        if (ownerCode == null || !ownerCode.matches("^OW-[A-Z0-9]{2,16}$")) {
            throw new IllegalArgumentException("货主编码格式错误，需为 OW- 前缀 + 2~16位大写字母/数字");
        }
    }

    public void disable() { this.status = 0; }
    public void enable() { this.status = 1; }
    public boolean isEnabled() { return status != null && status == 1; }
}
