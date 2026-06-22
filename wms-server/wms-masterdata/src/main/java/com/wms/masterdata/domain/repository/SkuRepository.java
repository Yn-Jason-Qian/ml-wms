package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Sku;
import java.util.List;
import java.util.Optional;

public interface SkuRepository {
    Optional<Sku> findById(Long id);
    Optional<Sku> findByCode(Long tenantId, String skuCode);
    List<Sku> findByOwner(Long tenantId, Long ownerId);
    List<Sku> findAll(Long tenantId);
    void save(Sku sku);
    void update(Sku sku);
    void deleteById(Long id);
    boolean existsByCode(Long tenantId, String skuCode, Long excludeId);
}
