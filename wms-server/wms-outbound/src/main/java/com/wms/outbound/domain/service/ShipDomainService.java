package com.wms.outbound.domain.service;

import com.wms.outbound.domain.entity.PickLine;
import com.wms.outbound.domain.gateway.InventoryGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipDomainService {
    private final InventoryGateway inventoryGateway;

    /** 发货确认：扣除库存 + 写流水 */
    public void shipDeduct(PickLine pickLine, Long userId) {
        inventoryGateway.deductStock(
                pickLine.getTenantId(),
                null,
                pickLine.getLocationId(),
                pickLine.getSkuId(),
                pickLine.getBatchNo(),
                pickLine.getPickedQty(),
                "SHIP",
                pickLine.getPickHeaderId().toString(),
                pickLine.getPickHeaderId(),
                userId);
    }
}
