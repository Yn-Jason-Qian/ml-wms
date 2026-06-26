package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_ship_header")
public class ShipHeader extends BaseEntity {

    /** 发货状态 */
    public enum Status {
        CREATED,
        SHIPPING,
        DONE
    }

    /** 配送方式 */
    public enum DeliveryMethod {
        EXPRESS,
        LTL,
        FTL,
        SELF_PICKUP
    }

    private Long warehouseId;
    private Long ownerId;
    private String shipNo;
    private Long waveHeaderId;
    private DeliveryMethod deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private Integer packageCount;
    private BigDecimal grossWeight;
    private BigDecimal volume;
    private Long shipBy;
    private LocalDateTime shipAt;
    private Status status;
}
