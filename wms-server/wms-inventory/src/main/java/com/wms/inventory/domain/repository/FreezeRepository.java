package com.wms.inventory.domain.repository;

import com.wms.inventory.domain.entity.Freeze;

import java.util.List;
import java.util.Optional;

public interface FreezeRepository {
    Optional<Freeze> findById(Long id);

    List<Freeze> findByStatus(Long tenantId, String status);

    List<Freeze> findBySku(Long tenantId, Long skuId);

    void save(Freeze freeze);

    void update(Freeze freeze);
}
