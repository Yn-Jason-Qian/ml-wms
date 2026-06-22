package com.wms.print.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_print_template")
public class PrintTemplate extends BaseEntity {
    private String templateCode;
    private String templateName;
    private String templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
    private Integer status;
}
