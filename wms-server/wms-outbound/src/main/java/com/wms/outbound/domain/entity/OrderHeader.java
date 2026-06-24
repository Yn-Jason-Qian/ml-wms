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
    private Long warehouseId;
    private Long ownerId;
    private String orderNo;
    private String orderType;
    private String sourceNo;
    private Long waveHeaderId;
    private String customerName;
    private String customerAddress;
    private LocalDateTime expectedShipTime;
    private Integer priority;
    private String status;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_ALLOCATED = "ALLOCATED";
    public static final String STATUS_PICKING = "PICKING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_CHECKED = "CHECKED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}
