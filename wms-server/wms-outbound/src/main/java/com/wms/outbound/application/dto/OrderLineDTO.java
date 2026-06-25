package com.wms.outbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineDTO {
    private Long id;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal orderQty;
    private BigDecimal allocatedQty;
    private BigDecimal pickedQty;
    private BigDecimal shippedQty;
    private String batchNo;
    private String status;
}
