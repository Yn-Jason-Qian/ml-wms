package com.wms.inventory.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_freeze")
public class Freeze extends BaseEntity {

    private Long warehouseId;
    private String freezeType;
    private Long stockId;
    private Long skuId;
    private Long locationId;
    private String batchNo;
    private BigDecimal freezeQty;
    private String reason;
    private String status;
    private Long freezeBy;
    private LocalDateTime freezeAt;
    private Long releaseBy;
    private LocalDateTime releaseAt;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RELEASED = "RELEASED";

    public void release() {
        this.status = STATUS_RELEASED;
        this.releaseAt = LocalDateTime.now();
    }
}
