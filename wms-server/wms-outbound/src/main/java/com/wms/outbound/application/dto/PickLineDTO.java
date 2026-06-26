package com.wms.outbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PickLineDTO {
    private Long id;
    private Long pickHeaderId;
    private Integer lineNo;
    private Long orderHeaderId;
    private Long orderLineId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal pickQty;
    private BigDecimal pickedQty;
    private Long locationId;
    private String batchNo;
    private String lotAttrs;
    private String toContainer;
    private String status;
}
