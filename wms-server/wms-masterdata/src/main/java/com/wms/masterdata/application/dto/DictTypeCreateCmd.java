package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class DictTypeCreateCmd {
    @NotBlank
    @Size(max = 64)
    private String typeCode;

    @NotBlank
    @Size(max = 128)
    private String typeName;
}
