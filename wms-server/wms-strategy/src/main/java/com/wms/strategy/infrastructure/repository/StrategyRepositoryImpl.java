package com.wms.strategy.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;
import com.wms.strategy.domain.repository.StrategyRepository;
import com.wms.strategy.infrastructure.mapper.StrategyConfigMapper;
import com.wms.strategy.infrastructure.mapper.StrategyRuleMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StrategyRepositoryImpl implements StrategyRepository {
    private final StrategyConfigMapper configMapper;
    private final StrategyRuleMapper ruleMapper;

    @Override
    public Optional<StrategyConfig> findById(Long id) {
        return Optional.ofNullable(configMapper.selectById(id));
    }

    @Override
    public Optional<StrategyConfig> findByCode(Long tenantId, String code) {
        return Optional.ofNullable(
                configMapper.selectOne(
                        new LambdaQueryWrapper<StrategyConfig>()
                                .eq(StrategyConfig::getTenantId, tenantId)
                                .eq(StrategyConfig::getStrategyCode, code)));
    }

    @Override
    public List<StrategyConfig> findByType(Long tenantId, String type) {
        return configMapper.selectList(
                new LambdaQueryWrapper<StrategyConfig>()
                        .eq(StrategyConfig::getTenantId, tenantId)
                        .eq(StrategyConfig::getStrategyType, type)
                        .eq(StrategyConfig::getIsEnabled, 1)
                        .orderByAsc(StrategyConfig::getSortOrder));
    }

    @Override
    public List<StrategyConfig> findAll(Long tenantId) {
        return configMapper.selectList(
                new LambdaQueryWrapper<StrategyConfig>().eq(StrategyConfig::getTenantId, tenantId));
    }

    @Override
    public void saveConfig(StrategyConfig c) {
        configMapper.insert(c);
    }

    @Override
    public void updateConfig(StrategyConfig c) {
        configMapper.updateById(c);
    }

    @Override
    public void deleteConfig(Long id) {
        configMapper.deleteById(id);
    }

    @Override
    public List<StrategyRule> findRulesByStrategy(Long strategyId) {
        return ruleMapper.selectList(
                new LambdaQueryWrapper<StrategyRule>()
                        .eq(StrategyRule::getStrategyId, strategyId)
                        .eq(StrategyRule::getIsEnabled, 1)
                        .orderByAsc(StrategyRule::getRuleNo));
    }

    @Override
    public Optional<StrategyRule> findRuleById(Long id) {
        return Optional.ofNullable(ruleMapper.selectById(id));
    }

    @Override
    public void saveRule(StrategyRule r) {
        ruleMapper.insert(r);
    }

    @Override
    public void updateRule(StrategyRule r) {
        ruleMapper.updateById(r);
    }

    @Override
    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(Long tenantId, String code, Long excludeId) {
        Long count =
                configMapper.selectCount(
                        new LambdaQueryWrapper<StrategyConfig>()
                                .eq(StrategyConfig::getTenantId, tenantId)
                                .eq(StrategyConfig::getStrategyCode, code)
                                .ne(excludeId != null, StrategyConfig::getId, excludeId));
        return count > 0;
    }
}
