package com.wms.masterdata.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.masterdata.domain.entity.SkuPackage;
import com.wms.masterdata.domain.repository.SkuPackageRepository;
import com.wms.masterdata.domain.repository.SkuRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuDomainService {
    private final SkuRepository skuRepository;
    private final SkuPackageRepository skuPackageRepository;

    public void validateCreate(Sku sku) {
        if (skuRepository.existsByCode(sku.getTenantId(), sku.getSkuCode(), null)) {
            throw BusinessException.conflict("SKU编码 [" + sku.getSkuCode() + "] 已存在");
        }
    }

    public void validateUpdate(Sku sku) {
        if (skuRepository.existsByCode(sku.getTenantId(), sku.getSkuCode(), sku.getId())) {
            throw BusinessException.conflict("SKU编码 [" + sku.getSkuCode() + "] 已存在");
        }
    }

    public void disableSku(Sku sku) {
        if (!sku.isEnabled()) throw new BusinessException("SKU已处于禁用状态");
        sku.disable();
    }

    public void enableSku(Sku sku) {
        if (sku.isEnabled()) throw new BusinessException("SKU已处于启用状态");
        sku.enable();
    }

    /** 包装规格校验：同一SKU下层级不可重复 */
    public void validatePackageCreate(SkuPackage pkg) {
        if (skuPackageRepository.existsByLevel(
                pkg.getTenantId(), pkg.getSkuId(), pkg.getPackageLevel(), null)) {
            throw BusinessException.conflict("包装层级 [" + pkg.getPackageLevel() + "] 已存在");
        }
    }

    public void validatePackageUpdate(SkuPackage pkg) {
        if (skuPackageRepository.existsByLevel(
                pkg.getTenantId(), pkg.getSkuId(), pkg.getPackageLevel(), pkg.getId())) {
            throw BusinessException.conflict("包装层级 [" + pkg.getPackageLevel() + "] 已存在");
        }
    }
}
