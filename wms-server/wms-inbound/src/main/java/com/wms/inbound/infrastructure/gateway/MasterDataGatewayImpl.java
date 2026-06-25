package com.wms.inbound.infrastructure.gateway;

import com.wms.inbound.domain.gateway.MasterDataGateway;
import com.wms.masterdata.application.service.SkuAppService;
import com.wms.masterdata.domain.entity.Sku;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component("inboundMasterDataGateway")
@RequiredArgsConstructor
public class MasterDataGatewayImpl implements MasterDataGateway {

    private final SkuAppService skuAppService;

    @Override
    public Sku resolveSku(Long skuId, String skuCode, Long tenantId) {
        return skuAppService.resolveSku(skuId, skuCode, tenantId);
    }
}
