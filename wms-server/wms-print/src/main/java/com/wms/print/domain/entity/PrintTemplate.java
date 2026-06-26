package com.wms.print.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_print_template")
public class PrintTemplate extends BaseEntity {

    /** 模板类型 */
    public enum TemplateType {
        SKU_LABEL,
        LOCATION_LABEL,
        PALLET_LABEL,
        INBOUND_LABEL,
        SHIPPING_LABEL
    }

    private String templateCode;
    private String templateName;
    private TemplateType templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
    private Integer status;
}
