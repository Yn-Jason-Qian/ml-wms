package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.MoveHeader.MoveType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoveCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private MoveType moveType;
    @NotNull private Long skuId;
    @NotNull private BigDecimal moveQty;
    @NotNull private Long fromLocationId;
    @NotNull private Long toLocationId;
    private String batchNo;
    private String remark;
}
