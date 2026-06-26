package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_order_header")
public class OrderHeader extends BaseEntity {

    /** 订单类型 */
    public enum OrderType {
        SALE,
        TRANSFER,
        RETURN_SUPPLIER,
        SAMPLE
    }

    /** 订单状态 */
    public enum Status {
        CREATED,
        ALLOCATED,
        PICKING,
        PICKED,
        CHECKED,
        SHIPPED,
        CANCELLED
    }

    private Long warehouseId;
    private Long ownerId;
    private String orderNo;
    private OrderType orderType;
    private String sourceNo;
    private Long waveHeaderId;
    private String customerName;
    private String customerAddress;
    private LocalDateTime expectedShipTime;
    private Integer priority;
    private Status status;
    private String remark;
}
