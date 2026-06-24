package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_move_header")
public class MoveHeader extends BaseEntity {

    private Long warehouseId;
    private String moveNo;
    private String moveType;
    private String status;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_MOVING = "MOVING";
    public static final String STATUS_DONE = "DONE";
}
