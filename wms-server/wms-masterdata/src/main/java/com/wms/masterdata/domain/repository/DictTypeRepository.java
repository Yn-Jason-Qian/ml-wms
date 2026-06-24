package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.DictType;

import java.util.List;
import java.util.Optional;

public interface DictTypeRepository {
    List<DictType> findAll(Long tenantId);

    Optional<DictType> findByCode(Long tenantId, String typeCode);

    void save(DictType dictType);

    void update(DictType dictType);

    void deleteById(Long id);
}
