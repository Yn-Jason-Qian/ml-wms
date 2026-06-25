package com.wms.web.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpiryAlertDTO {
    private String skuCode;
    private String skuName;
    private String batchNo;
    private Long locationId;
    private LocalDateTime expiryDate;
    private BigDecimal qtyOnHand;
    private Integer daysLeft;
}
