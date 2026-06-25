package com.wms.inbound.infrastructure.gateway;

import com.wms.inbound.domain.gateway.InventoryGateway;
import com.wms.inventory.application.service.StockAppService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("inboundInventoryGateway")
@RequiredArgsConstructor
public class InventoryGatewayImpl implements InventoryGateway {

    private final StockAppService stockAppService;

    @Override
    public void increaseStock(
            Long tenantId,
            Long warehouseId,
            Long ownerId,
            Long locationId,
            Long skuId,
            String skuCode,
            String skuName,
            String batchNo,
            String lotAttrs,
            BigDecimal qty,
            String txnType,
            String refNo,
            Long refId,
            Long userId) {
        stockAppService.increaseStock(
                tenantId,
                warehouseId,
                ownerId,
                locationId,
                skuId,
                skuCode,
                skuName,
                batchNo,
                lotAttrs,
                qty,
                txnType,
                refNo,
                refId,
                userId);
    }
}
