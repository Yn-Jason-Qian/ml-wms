package com.wms.outbound.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShipDTO {
    private Long id;
    private String shipNo;
    private Long warehouseId;
    private String deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private String status;
    private LocalDateTime createdAt;
}
