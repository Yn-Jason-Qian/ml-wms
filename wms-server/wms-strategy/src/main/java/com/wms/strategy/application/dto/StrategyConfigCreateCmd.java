package com.wms.strategy.application.dto;

import com.wms.strategy.domain.entity.StrategyConfig.StrategyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class StrategyConfigCreateCmd {
    @NotBlank
    @Size(max = 64)
    private String strategyCode;

    @NotBlank
    @Size(max = 128)
    private String strategyName;

    @NotNull private StrategyType strategyType;
    private String description;
    private Integer sortOrder;
}
