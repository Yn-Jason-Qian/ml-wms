package com.wms.outbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_check_line")
public class CheckLine extends BaseEntity {
    private Long checkHeaderId;
    private Integer lineNo;
    private Long orderHeaderId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal orderQty;
    private BigDecimal checkQty;
    private Integer isMatch;
    private String diffReason;
    private String fromContainer;
    private String toContainer;
}
