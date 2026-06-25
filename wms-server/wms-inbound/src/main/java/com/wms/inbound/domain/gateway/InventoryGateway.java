package com.wms.inbound.domain.gateway;

import java.math.BigDecimal;

/**
 * 入库域访问库存域的网关端口。
 *
 * <p>封装语义化的库存操作，避免直接依赖库存域的 Repository 或实体操作。
 */
public interface InventoryGateway {

    void increaseStock(
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
            Long userId);
}
