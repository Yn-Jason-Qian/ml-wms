package com.wms.outbound.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

/** PDA 复核确认 */
@Data
public class CheckSubmitCmd {
    @NotNull private Long checkHeaderId;
    @NotNull private Long checkLineId;
    @NotNull private BigDecimal checkQty;

    /** 是否一致: 0=差异 1=一致 */
    @NotNull private Integer isMatch;

    private String diffReason;
    private String fromContainer;
}
