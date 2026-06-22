package com.wms.inventory.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_stock_transaction")
public class StockTransaction extends BaseEntity {

    private Long warehouseId;
    private Long stockId;
    private Long skuId;
    private String skuCode;
    private String batchNo;
    private String txnType;
    private String txnDirection;
    private BigDecimal txnQty;
    private BigDecimal qtyBefore;
    private BigDecimal qtyAfter;
    private String refNo;
    private Long refId;
    private Long refLineId;
    private Long fromLocationId;
    private Long toLocationId;
    private String remark;
    private LocalDateTime txnTime;
}
