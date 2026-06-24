package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QcSubmitCmd {
    @NotNull private Long headerId;
    @NotNull private Long skuId;
    @NotNull private BigDecimal inspectQty;
    @NotNull private BigDecimal passQty;
    private BigDecimal rejectQty;
    private String rejectReason;
    private String batchNo;
}
