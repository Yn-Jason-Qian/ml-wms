package com.wms.outbound.infrastructure.gateway;

import com.wms.masterdata.application.service.SkuAppService;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.outbound.domain.gateway.MasterDataGateway;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterDataGatewayImpl implements MasterDataGateway {

    private final SkuAppService skuAppService;

    @Override
    public Sku resolveSku(Long skuId, String skuCode, Long tenantId) {
        return skuAppService.resolveSku(skuId, skuCode, tenantId);
    }
}
