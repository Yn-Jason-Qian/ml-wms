package com.wms.inbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QcLineDTO {
    private Long id;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal inspectQty;
    private BigDecimal passQty;
    private BigDecimal rejectQty;
    private String rejectReason;
    private String batchNo;
    private String lotAttrs;
}
