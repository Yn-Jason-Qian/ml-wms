package com.wms.outbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_order_line")
public class OrderLine extends BaseEntity {
    private Long orderHeaderId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal orderQty;
    private BigDecimal allocatedQty;
    private BigDecimal pickedQty;
    private BigDecimal shippedQty;
    private Long unitId;
    private String batchNo;
    private String lotAttrs;
    private String status;

    public BigDecimal getRemainingQty() { return orderQty.subtract(allocatedQty); }
}
