package com.wms.print.application.dto;

import com.wms.print.domain.entity.PrintTemplate.TemplateType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrintTemplateCreateCmd {
    @NotBlank private String templateCode;
    @NotBlank private String templateName;
    @NotNull private TemplateType templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
}
