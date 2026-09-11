package com.wms.inbound.domain.gateway;

import com.wms.masterdata.domain.entity.Location;
import com.wms.masterdata.domain.entity.Sku;

import java.util.Collection;
import java.util.Map;

/**
 * 入库域访问主数据域的网关端口。
 *
 * <p>封装 SKU 解析逻辑，避免直接依赖主数据域 Repository。
 */
public interface MasterDataGateway {

    Sku resolveSku(Long skuId, String skuCode, Long tenantId);

    Location resolveLocation(Long locationId, String locationCode, Long warehouseId, Long tenantId);

    /** 批量查询库位编码，key = 库位 ID。 */
    Map<Long, String> resolveLocationCodes(Collection<Long> locationIds);
}
