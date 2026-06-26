package com.wms.outbound.infrastructure.gateway;

import com.wms.outbound.domain.gateway.StrategyGateway;
import com.wms.strategy.application.service.StrategyAppService;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.service.StrategyEngine;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("outboundStrategyGateway")
@RequiredArgsConstructor
public class StrategyGatewayImpl implements StrategyGateway {

    private final StrategyAppService strategyAppService;

    @Override
    public StrategyEngine.MatchResult matchAllocationStrategy(
            Long tenantId, Map<String, Object> context) {
        return strategyAppService.matchStrategy(
                tenantId, StrategyConfig.StrategyType.ALLOCATION.name(), context);
    }
}
