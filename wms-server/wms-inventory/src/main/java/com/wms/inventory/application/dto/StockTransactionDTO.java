package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.StockTransaction.TxnDirection;
import com.wms.inventory.domain.entity.StockTransaction.TxnType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockTransactionDTO {
    private Long id;
    private Long warehouseId;
    private Long stockId;
    private Long skuId;
    private String skuCode;
    private String batchNo;
    private TxnType txnType;
    private TxnDirection txnDirection;
    private BigDecimal txnQty;
    private BigDecimal qtyBefore;
    private BigDecimal qtyAfter;
    private String refNo;
    private Long refId;
    private Long fromLocationId;
    private Long toLocationId;
    private String remark;
    private LocalDateTime txnTime;
}
