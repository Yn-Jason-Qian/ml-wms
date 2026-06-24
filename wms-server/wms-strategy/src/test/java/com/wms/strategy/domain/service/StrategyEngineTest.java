package com.wms.strategy.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

class StrategyEngineTest {

    private StrategyEngine engine;
    private List<StrategyConfig> testConfigs;

    @BeforeEach
    void setUp() {
        engine = new StrategyEngine(new ObjectMapper());

        StrategyRule rule = new StrategyRule();
        rule.setRuleNo(1);
        rule.setRuleName("重量匹配");
        rule.setConditionsJson("[{\"attr\":\"sku.weight\",\"op\":\"lte\",\"value\":\"500\"}]");
        rule.setActionsJson("[{\"type\":\"assign_zone\",\"params\":{\"zone\":\"LIGHT\"}}]");
        rule.setIsEnabled(1);

        StrategyConfig config = new StrategyConfig();
        config.setStrategyCode("PUTAWAY-LIGHT");
        config.setStrategyName("轻货上架策略");
        config.setStrategyType("PUTAWAY");
        config.setSortOrder(1);
        config.setIsEnabled(1);
        config.setRules(new ArrayList<>(List.of(rule)));

        testConfigs = new ArrayList<>(List.of(config));
    }

    @Test
    void testMatchSuccess() {
        Function<String, List<StrategyConfig>> provider =
                type -> "PUTAWAY".equals(type) ? testConfigs : new ArrayList<>();

        Map<String, Object> sku = new HashMap<>();
        sku.put("weight", 300);
        var ctx = Map.<String, Object>of("sku", sku);
        var result = engine.match("PUTAWAY", ctx, provider);

        assertNotNull(result);
        assertEquals("PUTAWAY-LIGHT", result.config().getStrategyCode());
        assertEquals(1, result.rule().getRuleNo());
        assertEquals("assign_zone", result.getActionType());
        assertEquals("LIGHT", result.getActionParams().get("zone"));
    }

    @Test
    void testMatchNoMatch() {
        Function<String, List<StrategyConfig>> provider =
                type -> "PUTAWAY".equals(type) ? testConfigs : new ArrayList<>();

        Map<String, Object> sku2 = new HashMap<>();
        sku2.put("weight", 800);
        var ctx2 = Map.<String, Object>of("sku", sku2);
        var result = engine.match("PUTAWAY", ctx2, provider);
        assertNull(result);
    }

    @Test
    void testNoConfigReturnsNull() {
        var result = engine.match("PUTAWAY", Map.of(), type -> new ArrayList<>());
        assertNull(result);
    }

    @Test
    void testEmptyConditionsAlwaysMatches() {
        var emptyRule = new StrategyRule();
        emptyRule.setRuleNo(1);
        emptyRule.setRuleName("无条件");
        emptyRule.setConditionsJson("[]");
        emptyRule.setActionsJson("[{\"type\":\"default\",\"params\":{}}]");
        emptyRule.setIsEnabled(1);

        var config = new StrategyConfig();
        config.setStrategyCode("DEFAULT");
        config.setStrategyType("DEFAULT");
        config.setSortOrder(1);
        config.setIsEnabled(1);
        config.setRules(new ArrayList<>(List.of(emptyRule)));

        var result = engine.match("DEFAULT", Map.of(), type -> new ArrayList<>(List.of(config)));
        assertNotNull(result);
    }

    @Test
    void testDisabledConfigSkipped() {
        var rule = new StrategyRule();
        rule.setRuleNo(1);
        rule.setRuleName("rule");
        rule.setConditionsJson("[]");
        rule.setActionsJson("[{\"type\":\"test\"}]");
        rule.setIsEnabled(1);

        var config = new StrategyConfig();
        config.setStrategyCode("DISABLED");
        config.setSortOrder(1);
        config.setIsEnabled(0);
        config.setRules(new ArrayList<>(List.of(rule)));

        var result = engine.match("PUTAWAY", Map.of(), type -> new ArrayList<>(List.of(config)));
        assertNull(result);
    }
}
