package com.wms.outbound.domain.gateway;

/**
 * 出库域访问库存域的网关端口。
 *
 * <p>封装语义化的库存操作，避免直接依赖库存域的 Repository 或 Stock 实体操作。
 */
public interface InventoryGateway {

    void allocateBySku(
            Long tenantId, Long skuId, java.math.BigDecimal requiredQty, String refNo, Long refId);

    void deductStock(
            Long tenantId,
            Long warehouseId,
            Long locationId,
            Long skuId,
            String batchNo,
            java.math.BigDecimal qty,
            String txnType,
            String refNo,
            Long refId,
            Long userId);
}
