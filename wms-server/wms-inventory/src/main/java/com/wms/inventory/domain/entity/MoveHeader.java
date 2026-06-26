package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_move_header")
public class MoveHeader extends BaseEntity {

    /** 库存移动类型 */
    public enum MoveType {
        MANUAL,
        REPLENISH,
        PUTAWAY_FIX,
        RETURN
    }

    /** 移动状态 */
    public enum Status {
        CREATED,
        MOVING,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private String moveNo;
    private MoveType moveType;
    private Status status;
    private String remark;
}
