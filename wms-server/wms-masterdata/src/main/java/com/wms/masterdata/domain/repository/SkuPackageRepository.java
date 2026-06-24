package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.SkuPackage;

import java.util.List;
import java.util.Optional;

public interface SkuPackageRepository {
    Optional<SkuPackage> findById(Long id);

    List<SkuPackage> findBySkuId(Long tenantId, Long skuId);

    void save(SkuPackage skuPackage);

    void update(SkuPackage skuPackage);

    void deleteById(Long id);

    boolean existsByLevel(Long tenantId, Long skuId, String packageLevel, Long excludeId);
}
