package com.wms.print.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PrintCmd {
    @NotNull private Long templateId;
    @NotBlank private String refType;
    @NotNull private Long refId;
    private String printerName;
    private Integer printCount;
}
