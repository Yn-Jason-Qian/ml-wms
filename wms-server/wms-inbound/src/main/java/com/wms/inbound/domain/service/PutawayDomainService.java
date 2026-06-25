package com.wms.inbound.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.inbound.domain.entity.PutawayLine;
import com.wms.inbound.domain.gateway.InventoryGateway;
import com.wms.inbound.domain.gateway.StrategyGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PutawayDomainService {
    private final InventoryGateway inventoryGateway;
    private final StrategyGateway strategyGateway;

    /** 执行上架 + 增加库存 + 写流水 */
    public void executePutaway(PutawayLine line, Long warehouseId, Long ownerId, Long userId) {
        if (line.getToLocationId() == null) {
            Map<String, Object> context = new HashMap<>();
            context.put("sku", Map.of("code", line.getSkuCode()));
            context.put("putaway", Map.of("fromLocationId", line.getFromLocationId()));
            var result = strategyGateway.matchPutawayStrategy(line.getTenantId(), context);
            if (result != null && result.getActionParams().containsKey("locationId")) {
                line.setToLocationId(
                        Long.valueOf(result.getActionParams().get("locationId").toString()));
            }
        }

        if (line.getToLocationId() == null) {
            throw BusinessException.notFound("未找到合适的上架库位，请手动指定");
        }

        inventoryGateway.increaseStock(
                line.getTenantId(),
                warehouseId,
                ownerId,
                line.getToLocationId(),
                line.getSkuId(),
                line.getSkuCode(),
                line.getSkuName(),
                line.getBatchNo(),
                line.getLotAttrs(),
                line.getPutawayQty(),
                "PUTAWAY",
                line.getPutawayHeaderId().toString(),
                line.getPutawayHeaderId(),
                userId);
    }
}
