package com.wms.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FreezeCreateCmd {
    @NotNull private Long warehouseId;
    @NotBlank private String freezeType;
    private Long stockId;
    private Long skuId;
    private Long locationId;
    private String batchNo;
    @NotNull private BigDecimal freezeQty;
    @NotBlank private String reason;
}
