package com.wms.strategy.application.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.util.Map;

/** 策略匹配请求：传入策略类型 + 上下文属性 */
@Data
public class StrategyMatchRequest {
    @NotBlank private String strategyType;
    private Map<String, Object> context;
}
