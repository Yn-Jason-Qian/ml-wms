package com.wms.print.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrintTemplateCreateCmd {
    @NotBlank private String templateCode;
    @NotBlank private String templateName;
    @NotBlank private String templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
}
