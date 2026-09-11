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

    /** 库位编码（详情接口按 locationId 反查填充） */
    private String locationCode;

    private String batchNo;
    private String lotAttrs;
    private String toContainer;
    private String status;
}
