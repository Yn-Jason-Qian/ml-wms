package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_freeze")
public class Freeze extends BaseEntity {

    /** 冻结类型 */
    public enum FreezeType {
        MANUAL,
        LOT_EXPIRY,
        QC_HOLD,
        DAMAGE
    }

    /** 冻结状态 */
    public enum Status {
        ACTIVE,
        RELEASED
    }

    private Long warehouseId;
    private FreezeType freezeType;
    private Long stockId;
    private Long skuId;
    private Long locationId;
    private String batchNo;
    private BigDecimal freezeQty;
    private String reason;
    private Status status;
    private Long freezeBy;
    private LocalDateTime freezeAt;
    private Long releaseBy;
    private LocalDateTime releaseAt;

    public void release() {
        this.status = Status.RELEASED;
        this.releaseAt = LocalDateTime.now();
    }
}
