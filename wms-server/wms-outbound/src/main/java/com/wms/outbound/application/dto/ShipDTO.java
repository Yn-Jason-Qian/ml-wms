package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.ShipHeader.DeliveryMethod;
import com.wms.outbound.domain.entity.ShipHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShipDTO {
    private Long id;
    private String shipNo;
    private Long warehouseId;
    private DeliveryMethod deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private Status status;
    private LocalDateTime createdAt;
}
