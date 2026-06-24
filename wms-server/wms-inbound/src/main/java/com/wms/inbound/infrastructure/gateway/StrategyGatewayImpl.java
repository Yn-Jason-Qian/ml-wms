package com.wms.inbound.infrastructure.gateway;

import com.wms.inbound.domain.gateway.StrategyGateway;
import com.wms.strategy.application.service.StrategyAppService;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.service.StrategyEngine;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StrategyGatewayImpl implements StrategyGateway {

    private final StrategyAppService strategyAppService;

    @Override
    public StrategyEngine.MatchResult matchPutawayStrategy(
            Long tenantId, Map<String, Object> context) {
        return strategyAppService.matchStrategy(tenantId, StrategyConfig.TYPE_PUTAWAY, context);
    }
}
