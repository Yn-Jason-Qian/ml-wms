package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.ShipHeader.DeliveryMethod;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long ownerId;
    @NotNull private Long waveHeaderId;
    @NotNull private DeliveryMethod deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private Integer packageCount;
    private BigDecimal grossWeight;
    private BigDecimal volume;
}
