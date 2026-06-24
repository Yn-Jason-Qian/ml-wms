package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_move_line")
public class MoveLine extends BaseEntity {

    private Long moveHeaderId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal moveQty;
    private Long fromLocationId;
    private Long fromStockId;
    private Long toLocationId;
    private Long toStockId;
    private String batchNo;
    private String lotAttrs;
    private String status;
    private Long moveBy;
    private LocalDateTime moveAt;
}
