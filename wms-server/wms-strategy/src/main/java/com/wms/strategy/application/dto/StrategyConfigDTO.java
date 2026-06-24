package com.wms.strategy.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StrategyConfigDTO {
    private Long id;
    private String strategyCode;
    private String strategyName;
    private String strategyType;
    private String description;
    private Integer sortOrder;
    private Integer isEnabled;
    private LocalDateTime createdAt;
    private List<StrategyRuleDTO> rules;
}
