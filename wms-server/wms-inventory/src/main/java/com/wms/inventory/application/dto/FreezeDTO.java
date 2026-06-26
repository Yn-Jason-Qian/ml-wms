package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.Freeze.FreezeType;
import com.wms.inventory.domain.entity.Freeze.Status;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FreezeDTO {
    private Long id;
    private Long warehouseId;
    private FreezeType freezeType;
    private Long stockId;
    private Long skuId;
    private Long locationId;
    private String batchNo;
    private BigDecimal freezeQty;
    private String reason;
    private Status status;
    private Long freezeBy;
    private LocalDateTime freezeAt;
    private LocalDateTime releaseAt;
}
