package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_qc_line")
public class QcLine extends BaseEntity {
    private Long qcHeaderId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal inspectQty;
    private BigDecimal passQty;
    private BigDecimal rejectQty;
    private String rejectReason;
    private String batchNo;
    private String lotAttrs;
}
