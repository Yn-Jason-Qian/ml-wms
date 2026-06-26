package com.wms.print.application.dto;

import com.wms.print.domain.entity.PrintRecord.RefType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PrintCmd {
    @NotNull private Long templateId;
    @NotNull private RefType refType;
    @NotNull private Long refId;
    private String printerName;
    private Integer printCount;
}
