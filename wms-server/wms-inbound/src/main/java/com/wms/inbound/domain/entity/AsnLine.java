package com.wms.inbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_asn_line")
public class AsnLine extends BaseEntity {
    private Long asnHeaderId;
    private Integer lineNo;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private Long unitId;
    private String batchNo;
    private String lotAttrs;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String status;

    /** 计算本行剩余可收数量 */
    public BigDecimal getRemainingQty() {
        return expectedQty.subtract(receivedQty);
    }
}
