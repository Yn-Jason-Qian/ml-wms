package com.wms.inventory.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

/** PDA 提交盘点结果 */
@Data
public class CountSubmitCmd {
    @NotNull private Long lineId;
    @NotNull private BigDecimal countQty;

    /** 1=一盘点, 2=二盘点 */
    @NotNull private Integer countRound;
}
