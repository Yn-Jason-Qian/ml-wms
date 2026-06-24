package com.wms.inbound.infrastructure.gateway;

import com.wms.inbound.domain.gateway.InventoryGateway;
import com.wms.inventory.application.service.InventoryAppService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class InventoryGatewayImpl implements InventoryGateway {

    private final InventoryAppService inventoryAppService;

    @Override
    public void increaseStock(
            Long tenantId,
            Long warehouseId,
            Long locationId,
            Long skuId,
            String skuCode,
            String skuName,
            String batchNo,
            String lotAttrs,
            BigDecimal qty,
            String refNo,
            Long refId,
            Long userId) {
        inventoryAppService.increaseStock(
                tenantId, warehouseId, locationId, skuId, skuCode, skuName, batchNo, lotAttrs,
                qty, refNo, refId, userId);
    }
}
