package com.wms.inbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PutawayLineDTO {
    private Long id;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal putawayQty;
    private BigDecimal doneQty;
    private Long fromLocationId;

    /** 来源库位编码（详情接口按 fromLocationId 反查填充） */
    private String fromLocationCode;

    private Long toLocationId;
    private String batchNo;
    private String lotAttrs;
    private String status;
    private LocalDateTime putawayAt;
}
