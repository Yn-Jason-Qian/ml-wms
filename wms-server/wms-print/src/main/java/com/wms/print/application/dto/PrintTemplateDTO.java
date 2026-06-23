package com.wms.print.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PrintTemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateType;
    private BigDecimal paperWidth;
    private BigDecimal paperHeight;
    private String contentJson;
    private Integer status;
    private LocalDateTime createdAt;
}
