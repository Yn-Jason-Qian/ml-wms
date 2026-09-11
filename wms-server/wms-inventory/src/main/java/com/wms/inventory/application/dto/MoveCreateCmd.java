package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.MoveHeader.MoveType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoveCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private MoveType moveType;
    @NotNull private BigDecimal moveQty;

    /** SKU ID，与 skuCode 二选一（PDA 扫码传编码） */
    private Long skuId;

    private String skuCode;

    /** 来源库位 ID，与 fromLocationCode 二选一 */
    private Long fromLocationId;

    private String fromLocationCode;

    /** 目标库位 ID，与 toLocationCode 二选一 */
    private Long toLocationId;

    private String toLocationCode;

    private String batchNo;
    private String remark;
}
