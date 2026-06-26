package com.wms.print.application.dto;

import com.wms.print.domain.entity.PrintTemplate.TemplateType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PrintTemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
    private TemplateType templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
    private Integer status;
    private LocalDateTime createdAt;
}
