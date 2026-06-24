package com.wms.strategy.application.service;

import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.strategy.application.assembler.StrategyAssembler;
import com.wms.strategy.application.dto.*;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;
import com.wms.strategy.domain.repository.StrategyRepository;
import com.wms.strategy.domain.service.StrategyEngine;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyAppService {
    private final StrategyRepository strategyRepository;
    private final StrategyEngine engine;
    private final StrategyAssembler assembler;

    // ───── 策略配置 CRUD ─────

    public List<StrategyConfigDTO> listByType(String strategyType) {
        Long tenantId = UserContext.getTenantId();
        return strategyRepository.findByType(tenantId, strategyType).stream()
                .map(
                        c -> {
                            List<StrategyRule> rules =
                                    strategyRepository.findRulesByStrategy(c.getId());
                            return assembler.toDTO(c, rules);
                        })
                .collect(Collectors.toList());
    }

    public List<StrategyConfigDTO> listAll() {
        return strategyRepository.findAll(UserContext.getTenantId()).stream()
                .map(c -> assembler.toDTO(c, null))
                .collect(Collectors.toList());
    }

    public StrategyConfigDTO getById(Long id) {
        StrategyConfig c =
                strategyRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("策略不存在"));
        List<StrategyRule> rules = strategyRepository.findRulesByStrategy(id);
        return assembler.toDTO(c, rules);
    }

    @Transactional
    public StrategyConfigDTO createConfig(StrategyConfigCreateCmd cmd) {
        StrategyConfig c = assembler.toEntity(cmd);
        c.setTenantId(UserContext.getTenantId());
        c.setCreatedBy(UserContext.getUserId());
        c.setUpdatedBy(UserContext.getUserId());
        if (strategyRepository.existsByCode(c.getTenantId(), c.getStrategyCode(), null)) {
            throw BusinessException.conflict("策略编码已存在");
        }
        strategyRepository.saveConfig(c);
        return assembler.toDTO(c, List.of());
    }

    @Transactional
    public void deleteConfig(Long id) {
        strategyRepository.deleteConfig(id);
    }

    // ───── 规则管理 ─────

    @Transactional
    public StrategyRuleDTO addRule(StrategyRuleCreateCmd cmd) {
        StrategyRule r = assembler.toEntity(cmd);
        r.setTenantId(UserContext.getTenantId());
        r.setCreatedBy(UserContext.getUserId());
        r.setUpdatedBy(UserContext.getUserId());
        strategyRepository.saveRule(r);
        return assembler.toDTO(r);
    }

    @Transactional
    public void updateRule(Long id, StrategyRuleCreateCmd cmd) {
        StrategyRule r =
                strategyRepository
                        .findRuleById(id)
                        .orElseThrow(() -> BusinessException.notFound("规则不存在"));
        r.setRuleNo(cmd.getRuleNo());
        r.setRuleName(cmd.getRuleName());
        r.setConditionsJson(cmd.getConditionsJson());
        r.setActionsJson(cmd.getActionsJson());
        r.setUpdatedBy(UserContext.getUserId());
        strategyRepository.updateRule(r);
    }

    @Transactional
    public void deleteRule(Long id) {
        strategyRepository.deleteRule(id);
    }

    // ───── 策略匹配 ─────

    public StrategyMatchResultDTO match(StrategyMatchRequest request) {
        Long tenantId = UserContext.getTenantId();
        Map<String, Object> context =
                request.getContext() != null ? request.getContext() : Map.of();

        StrategyEngine.MatchResult result =
                engine.match(
                        request.getStrategyType(),
                        context,
                        type ->
                                strategyRepository.findByType(tenantId, type).stream()
                                        .peek(
                                                c ->
                                                        c.setRules(
                                                                strategyRepository
                                                                        .findRulesByStrategy(
                                                                                c.getId())))
                                        .collect(Collectors.toList()));

        StrategyMatchResultDTO dto = new StrategyMatchResultDTO();
        if (result != null) {
            dto.setMatched(true);
            dto.setStrategyCode(result.config().getStrategyCode());
            dto.setStrategyName(result.config().getStrategyName());
            dto.setRuleNo(result.rule().getRuleNo());
            dto.setRuleName(result.rule().getRuleName());
            dto.setActionType(result.getActionType());
            dto.setActionParams(result.getActionParams());
        } else {
            dto.setMatched(false);
        }
        return dto;
    }
}
