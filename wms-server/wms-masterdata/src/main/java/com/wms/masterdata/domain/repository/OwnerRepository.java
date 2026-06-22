package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Owner;
import java.util.List;
import java.util.Optional;

public interface OwnerRepository {
    Optional<Owner> findById(Long id);
    List<Owner> findAll(Long tenantId);
    void save(Owner owner);
    void update(Owner owner);
    void deleteById(Long id);
    boolean existsByCode(Long tenantId, String ownerCode, Long excludeId);
}
