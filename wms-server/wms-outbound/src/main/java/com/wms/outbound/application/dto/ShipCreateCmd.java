package com.wms.outbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long ownerId;
    @NotNull private Long waveHeaderId;
    @NotBlank private String deliveryMethod;
    private String carrierName;
    private String trackingNo;
    private Integer packageCount;
    private BigDecimal grossWeight;
    private BigDecimal volume;
}
