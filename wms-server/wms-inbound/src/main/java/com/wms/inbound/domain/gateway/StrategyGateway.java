package com.wms.inbound.domain.gateway;

import com.wms.strategy.domain.service.StrategyEngine;

import java.util.Map;

/**
 * 入库域访问策略域的网关端口。
 *
 * <p>封装上架策略匹配逻辑，避免直接依赖策略域 Repository。
 */
public interface StrategyGateway {

    StrategyEngine.MatchResult matchPutawayStrategy(Long tenantId, Map<String, Object> context);
}
