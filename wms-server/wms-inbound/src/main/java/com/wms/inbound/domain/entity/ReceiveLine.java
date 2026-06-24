package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_receive_line")
public class ReceiveLine extends BaseEntity {
    private Long receiveHeaderId;
    private Long asnLineId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal receiveQty;
    private String receivePackage;
    private Long unitId;
    private Long receiveLocationId;
    private String batchNo;
    private String lotAttrs;
    private LocalDate productionDate;
    private LocalDate expiryDate;
}
