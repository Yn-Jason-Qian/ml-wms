package com.wms.masterdata.application.dto;

import com.wms.masterdata.domain.entity.Area.AreaType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AreaUpdateCmd {
    @NotNull private Long id;

    @NotBlank
    @Size(min = 5, max = 19)
    private String areaCode;

    @NotBlank
    @Size(max = 128)
    private String areaName;

    @NotNull private AreaType areaType;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
}
