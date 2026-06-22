package com.wms.masterdata.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_dictionary")
public class Dictionary extends BaseEntity {

    private String dictType;
    private String dictCode;
    private String dictName;
    private String parentCode;
    private Integer sortOrder;
    private String extra;
    private Integer status;

    public void disable() { this.status = 0; }
    public void enable() { this.status = 1; }
    public boolean isEnabled() { return status != null && status == 1; }
}
