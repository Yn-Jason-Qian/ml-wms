package com.wms.strategy.application.dto;

import lombok.Data;

@Data
public class StrategyRuleDTO {
    private Long id;
    private Long strategyId;
    private Integer ruleNo;
    private String ruleName;
    private String conditionsJson;
    private String actionsJson;
    private Integer isEnabled;
}
