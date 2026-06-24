package com.wms.inbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AsnLineDTO {
    private Long id;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private BigDecimal remainingQty;
    private String batchNo;
    private String lotAttrs;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String status;
}
