package com.wms.inbound.infrastructure.gateway;

import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.masterdata.application.service.LocationAppService;
import com.wms.masterdata.application.service.SkuAppService;
import com.wms.masterdata.domain.entity.Location;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component("inboundMasterDataGateway")
@RequiredArgsConstructor
public class MasterDataGatewayImpl implements MasterDataGateway {

    private final SkuAppService skuAppService;
    private final LocationAppService locationAppService;

    @Override
    public Sku resolveSku(Long skuId, String skuCode, Long tenantId) {
        return skuAppService.resolveSku(skuId, skuCode, tenantId);
    }

    @Override
    public Location resolveLocation(
            Long locationId, String locationCode, Long warehouseId, Long tenantId) {
        return locationAppService.resolveLocation(locationId, locationCode, warehouseId, tenantId);
    }

    @Override
    public Map<Long, String> resolveLocationCodes(Collection<Long> locationIds) {
        return locationAppService.findCodeMap(locationIds);
    }
}
