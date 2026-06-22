package com.wms.inventory.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_stocktake_line")
public class StocktakeLine extends BaseEntity {

    private Long stocktakeHeaderId;
    private Integer lineNo;
    private Long locationId;
    private String locationCode;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String batchNo;
    private BigDecimal bookQty;
    private BigDecimal firstCountQty;
    private Long firstCountBy;
    private LocalDateTime firstCountAt;
    private BigDecimal secondCountQty;
    private Long secondCountBy;
    private LocalDateTime secondCountAt;
    private BigDecimal diffQty;
    private BigDecimal adjQty;
    private String adjReason;
    private String status;
}
