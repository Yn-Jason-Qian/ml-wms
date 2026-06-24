package com.wms.strategy.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class StrategyRuleCreateCmd {
    @NotNull private Long strategyId;
    @NotNull private Integer ruleNo;
    @NotBlank private String ruleName;
    private String conditionsJson;
    @NotBlank private String actionsJson;
}
