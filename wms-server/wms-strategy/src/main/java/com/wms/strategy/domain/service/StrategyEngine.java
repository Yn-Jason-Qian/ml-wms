package com.wms.strategy.domain.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

/**
 * 策略引擎核心 —— 通用的条件匹配 + 动作执行框架。
 *
 * 规则格式示例：
 *   conditions: [{"attr":"sku.weight","op":"<=","value":"500"},
 *                {"attr":"location.type","op":"in","value":"RACK,SHELF"}]
 *   actions:    [{"type":"assign_location","params":{"prefix":"A","zone":"01"}}]
 *
 * 支持的运算符: eq, ne, gt, lt, gte, lte, in, notIn, contains, regex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyEngine {

    private final ObjectMapper objectMapper;

    /**
     * 对指定类型的策略进行匹配，返回第一条匹配成功的规则的动作。
     * 按策略的 sortOrder 升序、规则的 ruleNo 升序依次匹配。
     *
     * @param strategyType  策略类型 (PUTAWAY/ALLOCATION/WAVE/PICKING)
     * @param context       匹配上下文 (map of attribute → value)
     * @param configProvider 策略配置提供函数
     * @return 匹配结果 (规则 + 动作列表)，未匹配返回 null
     */
    public MatchResult match(String strategyType,
                              Map<String, Object> context,
                              Function<String, List<StrategyConfig>> configProvider) {

        List<StrategyConfig> configs = configProvider.apply(strategyType);
        if (configs == null || configs.isEmpty()) {
            log.debug("No strategy config found for type: {}", strategyType);
            return null;
        }

        // 按优先级排序
        configs.sort(Comparator.comparingInt(StrategyConfig::getSortOrder));

        for (StrategyConfig config : configs) {
            if (config.getIsEnabled() == 0) continue;

            List<StrategyRule> rules = config.getRules();
            if (rules == null) continue;
            rules.sort(Comparator.comparingInt(StrategyRule::getRuleNo));

            for (StrategyRule rule : rules) {
                if (rule.getIsEnabled() == 0) continue;
                if (evaluateConditions(rule.getConditionsJson(), context)) {
                    List<Map<String, Object>> actions = parseActions(rule.getActionsJson());
                    log.debug("Rule matched: config={}, rule={}", config.getStrategyCode(), rule.getRuleNo());
                    return new MatchResult(config, rule, actions);
                }
            }
        }
        return null;
    }

    /** 评估条件列表（AND 逻辑） */
    private boolean evaluateConditions(String conditionsJson, Map<String, Object> context) {
        if (StrUtil.isBlank(conditionsJson) || "[]".equals(conditionsJson.trim())) {
            return true; // 无条件，命中所有
        }
        try {
            List<Condition> conditions = objectMapper.readValue(conditionsJson,
                    new TypeReference<List<Condition>>() {});
            for (Condition cond : conditions) {
                Object actualValue = resolveValue(context, cond.getAttr());
                if (!evaluate(cond.getOp(), actualValue, cond.getValue())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to parse conditions: {}", conditionsJson, e);
            return false;
        }
    }

    /** 从上下文获取嵌套属性值，如 "sku.weight" → context.sku.weight */
    private Object resolveValue(Map<String, Object> context, String attr) {
        if (attr == null || !attr.contains(".")) {
            return context.get(attr);
        }
        String[] parts = attr.split("\\.");
        Object current = context;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private boolean evaluate(String op, Object actual, Object expected) {
        if (actual == null && expected == null) return "eq".equals(op);
        if (actual == null || expected == null) return false;

String actualStr = actual.toString();
String expectedStr = expected.toString();

        return switch (op) {
            case "eq" -> actualStr.equals(expectedStr);
            case "ne" -> !actualStr.equals(expectedStr);
            case "gt" -> compare(actualStr, expectedStr) > 0;
            case "lt" -> compare(actualStr, expectedStr) < 0;
            case "gte" -> compare(actualStr, expectedStr) >= 0;
            case "lte" -> compare(actualStr, expectedStr) <= 0;
            case "in" -> Arrays.asList(expectedStr.split(",")).contains(actualStr);
            case "notIn" -> !Arrays.asList(expectedStr.split(",")).contains(actualStr);
            case "contains" -> actualStr.contains(expectedStr);
            case "regex" -> actualStr.matches(expectedStr);
            default -> false;
        };
    }

    private int compare(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseActions(String actionsJson) {
        if (StrUtil.isBlank(actionsJson)) return Collections.emptyList();
        try {
            return objectMapper.readValue(actionsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse actions: {}", actionsJson, e);
            return Collections.emptyList();
        }
    }

    public record MatchResult(StrategyConfig config, StrategyRule rule, List<Map<String, Object>> actions) {
        public String getActionType() {
            if (actions != null && !actions.isEmpty()) {
                return (String) actions.get(0).get("type");
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getActionParams() {
            if (actions != null && !actions.isEmpty()) {
                return (Map<String, Object>) actions.get(0).getOrDefault("params", Map.of());
            }
            return Map.of();
        }
    }

    public static class Condition {
        private String attr;
        private String op;
        private String value;
        public String getAttr() { return attr; }
        public void setAttr(String attr) { this.attr = attr; }
        public String getOp() { return op; }
        public void setOp(String op) { this.op = op; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
