package com.wms.outbound.domain.service;

import com.wms.outbound.domain.entity.OrderLine;
import com.wms.outbound.domain.gateway.InventoryGateway;
import com.wms.outbound.domain.gateway.StrategyGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainService {
    private final InventoryGateway inventoryGateway;
    private final StrategyGateway strategyGateway;

    /** 库存分配（FIFO 优先） */
    public void allocateInventory(OrderLine orderLine, Long tenantId, Long warehouseId) {
        // 用策略引擎匹配分配策略
        Map<String, Object> context = new HashMap<>();
        context.put("order", Map.of("priority", "5"));
        Map<String, Object> skuCtx = new HashMap<>();
        skuCtx.put("code", orderLine.getSkuCode());
        if (orderLine.getBatchNo() != null) {
            skuCtx.put("batchNo", orderLine.getBatchNo());
        }
        context.put("sku", skuCtx);
        strategyGateway.matchAllocationStrategy(tenantId, context);

        // 委托库存域执行 FIFO 分配
        inventoryGateway.allocateBySku(
                tenantId,
                orderLine.getSkuId(),
                orderLine.getRemainingQty(),
                orderLine.getOrderHeaderId().toString(),
                orderLine.getOrderHeaderId());
    }
}
