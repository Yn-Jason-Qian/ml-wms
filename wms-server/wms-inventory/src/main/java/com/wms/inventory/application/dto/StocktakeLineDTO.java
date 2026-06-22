package com.wms.inventory.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StocktakeLineDTO {
    private Long id;
    private Integer lineNo;
    private Long locationId;
    private String locationCode;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String batchNo;
    private BigDecimal bookQty;
    private BigDecimal firstCountQty;
    private BigDecimal secondCountQty;
    private BigDecimal diffQty;
    private BigDecimal adjQty;
    private String adjReason;
    private String status;
}
