package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class DictionaryUpdateCmd {
    @NotNull private Long id;

    @NotBlank
    @Size(max = 64)
    private String dictCode;

    @NotBlank
    @Size(max = 128)
    private String dictName;

    private String parentCode;
    private Integer sortOrder;
    private String extra;
}
