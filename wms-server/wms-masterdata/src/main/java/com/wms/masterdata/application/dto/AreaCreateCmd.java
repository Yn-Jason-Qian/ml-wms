package com.wms.masterdata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AreaCreateCmd {
    @NotNull private Long warehouseId;
    @NotBlank @Size(min = 5, max = 19) private String areaCode;
    @NotBlank @Size(max = 128) private String areaName;
    @NotBlank private String areaType;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
}
