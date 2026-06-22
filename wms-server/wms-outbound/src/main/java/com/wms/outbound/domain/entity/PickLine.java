package com.wms.outbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_pick_line")
public class PickLine extends BaseEntity {
    private Long pickHeaderId;
    private Integer lineNo;
    private Long orderHeaderId;
    private Long orderLineId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal pickQty;
    private BigDecimal pickedQty;
    private Long locationId;
    private Long fromStockId;
    private String batchNo;
    private String lotAttrs;
    private String toContainer;
    private String status;
    private Long pickBy;
    private LocalDateTime pickAt;
}
