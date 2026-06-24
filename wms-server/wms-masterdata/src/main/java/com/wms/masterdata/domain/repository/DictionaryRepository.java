package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Dictionary;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository {
    Optional<Dictionary> findById(Long id);

    List<Dictionary> findByType(Long tenantId, String dictType);

    List<String> findAllTypes(Long tenantId);

    void save(Dictionary dict);

    void update(Dictionary dict);

    void deleteById(Long id);

    boolean existsByCode(Long tenantId, String dictType, String dictCode, Long excludeId);
}
