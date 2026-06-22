package com.wms.outbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_ship_header")
public class ShipHeader extends BaseEntity {
    private Long warehouseId;
    private Long ownerId;
    private String shipNo;
    private Long waveHeaderId;
    private String deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private Integer packageCount;
    private BigDecimal grossWeight;
    private BigDecimal volume;
    private Long shipBy;
    private LocalDateTime shipAt;
    private String status;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_DONE = "DONE";
}
