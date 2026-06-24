package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class OwnerUpdateCmd {
    @NotNull private Long id;

    @NotBlank
    @Size(min = 5, max = 19)
    private String ownerCode;

    @NotBlank
    @Size(max = 128)
    private String ownerName;

    private String contactPerson;
    private String contactPhone;
    private String address;
}
