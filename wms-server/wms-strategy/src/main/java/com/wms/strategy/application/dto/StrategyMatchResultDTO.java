package com.wms.strategy.application.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StrategyMatchResultDTO {
    private boolean matched;
    private String strategyCode;
    private String strategyName;
    private Integer ruleNo;
    private String ruleName;
    private String actionType;
    private Map<String, Object> actionParams;
}
