package com.wms.inventory.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDTO {
    private Long id;
    private Long warehouseId;
    private Long ownerId;
    private Long locationId;
    private String locationCode;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String batchNo;
    private String lotAttrs;
    private LocalDateTime productionDate;
    private LocalDateTime expiryDate;
    private BigDecimal qtyOnHand;
    private BigDecimal qtyAllocated;
    private BigDecimal qtyAvailable;
    private BigDecimal qtyFrozen;
    private Integer status;
    private LocalDateTime firstInTime;
    private LocalDateTime lastInTime;
}
