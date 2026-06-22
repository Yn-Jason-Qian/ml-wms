package com.wms.strategy.application.assembler;

import com.wms.strategy.application.dto.*;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.entity.StrategyRule;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class StrategyAssembler {

    public StrategyConfig toEntity(StrategyConfigCreateCmd cmd) {
        StrategyConfig c = new StrategyConfig();
        c.setStrategyCode(cmd.getStrategyCode()); c.setStrategyName(cmd.getStrategyName());
        c.setStrategyType(cmd.getStrategyType()); c.setDescription(cmd.getDescription());
        c.setSortOrder(cmd.getSortOrder() != null ? cmd.getSortOrder() : 0);
        c.setIsEnabled(1);
        return c;
    }

    public StrategyConfigDTO toDTO(StrategyConfig c, java.util.List<StrategyRule> rules) {
        StrategyConfigDTO d = new StrategyConfigDTO();
        d.setId(c.getId()); d.setStrategyCode(c.getStrategyCode());
        d.setStrategyName(c.getStrategyName()); d.setStrategyType(c.getStrategyType());
        d.setDescription(c.getDescription()); d.setSortOrder(c.getSortOrder());
        d.setIsEnabled(c.getIsEnabled()); d.setCreatedAt(c.getCreatedAt());
        if (rules != null) d.setRules(rules.stream().map(this::toDTO).collect(Collectors.toList()));
        return d;
    }

    public StrategyRule toEntity(StrategyRuleCreateCmd cmd) {
        StrategyRule r = new StrategyRule();
        r.setStrategyId(cmd.getStrategyId()); r.setRuleNo(cmd.getRuleNo());
        r.setRuleName(cmd.getRuleName()); r.setConditionsJson(cmd.getConditionsJson());
        r.setActionsJson(cmd.getActionsJson()); r.setIsEnabled(1);
        return r;
    }

    public StrategyRuleDTO toDTO(StrategyRule r) {
        StrategyRuleDTO d = new StrategyRuleDTO();
        d.setId(r.getId()); d.setStrategyId(r.getStrategyId());
        d.setRuleNo(r.getRuleNo()); d.setRuleName(r.getRuleName());
        d.setConditionsJson(r.getConditionsJson()); d.setActionsJson(r.getActionsJson());
        d.setIsEnabled(r.getIsEnabled());
        return d;
    }
}
