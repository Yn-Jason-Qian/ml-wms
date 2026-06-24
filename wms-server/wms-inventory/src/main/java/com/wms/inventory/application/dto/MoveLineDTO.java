package com.wms.inventory.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MoveLineDTO {
    private Long id;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal moveQty;
    private Long fromLocationId;
    private Long toLocationId;
    private String batchNo;
    private String status;
    private LocalDateTime moveAt;
}
