package com.wms.strategy.domain.repository;

import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;
import java.util.List;
import java.util.Optional;

public interface StrategyRepository {
    Optional<StrategyConfig> findById(Long id);
    Optional<StrategyConfig> findByCode(Long tenantId, String strategyCode);
    List<StrategyConfig> findByType(Long tenantId, String strategyType);
    List<StrategyConfig> findAll(Long tenantId);
    void saveConfig(StrategyConfig config);
    void updateConfig(StrategyConfig config);
    void deleteConfig(Long id);

    List<StrategyRule> findRulesByStrategy(Long strategyId);
    Optional<StrategyRule> findRuleById(Long id);
    void saveRule(StrategyRule rule);
    void updateRule(StrategyRule rule);
    void deleteRule(Long id);
    boolean existsByCode(Long tenantId, String strategyCode, Long excludeId);
}
