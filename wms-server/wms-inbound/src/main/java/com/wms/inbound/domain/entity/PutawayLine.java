package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_putaway_line")
public class PutawayLine extends BaseEntity {
    private Long putawayHeaderId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal putawayQty;
    private BigDecimal doneQty;
    private Long fromLocationId;
    private Long toLocationId;
    private String batchNo;
    private String lotAttrs;
    private String status;
    private Long putawayBy;
    private LocalDateTime putawayAt;
}
