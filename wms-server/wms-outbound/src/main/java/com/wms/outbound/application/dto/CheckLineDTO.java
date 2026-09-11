package com.wms.outbound.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckLineDTO {
    private Long id;
    private Long checkHeaderId;
    private Integer lineNo;
    private Long orderHeaderId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal orderQty;
    private BigDecimal checkQty;

    /** 是否一致: 0=差异 1=一致 null=未复核 */
    private Integer isMatch;

    private String diffReason;
    private String fromContainer;
    private String toContainer;
}
