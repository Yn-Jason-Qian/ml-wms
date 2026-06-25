package com.wms.outbound.infrastructure.gateway;

import com.wms.inventory.application.service.StockAppService;
import com.wms.outbound.domain.gateway.InventoryGateway;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("outboundInventoryGateway")
@RequiredArgsConstructor
public class InventoryGatewayImpl implements InventoryGateway {

    private final StockAppService stockAppService;

    @Override
    public void allocateBySku(
            Long tenantId, Long skuId, BigDecimal requiredQty, String refNo, Long refId) {
        stockAppService.allocateBySku(tenantId, skuId, requiredQty, refNo, refId);
    }

    @Override
    public Long findLocationBySku(Long tenantId, Long skuId, String batchNo) {
        return stockAppService.findLocationBySku(tenantId, skuId, batchNo);
    }

    @Override
    public void deductStock(
            Long tenantId,
            Long warehouseId,
            Long locationId,
            Long skuId,
            String batchNo,
            BigDecimal qty,
            String txnType,
            String refNo,
            Long refId,
            Long userId) {
        stockAppService.deductStock(
                tenantId,
                warehouseId,
                locationId,
                skuId,
                batchNo,
                qty,
                txnType,
                refNo,
                refId,
                userId);
    }
}
