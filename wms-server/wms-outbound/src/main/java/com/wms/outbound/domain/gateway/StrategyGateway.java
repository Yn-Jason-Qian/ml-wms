package com.wms.outbound.domain.gateway;

import com.wms.strategy.domain.service.StrategyEngine;

import java.util.Map;

/**
 * 出库域访问策略域的网关端口。
 *
 * <p>封装分配策略匹配逻辑，避免直接依赖策略域 Repository。
 */
public interface StrategyGateway {

    StrategyEngine.MatchResult matchAllocationStrategy(Long tenantId, Map<String, Object> context);
}
