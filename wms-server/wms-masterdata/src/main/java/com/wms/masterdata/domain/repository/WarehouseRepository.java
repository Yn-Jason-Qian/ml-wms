package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Warehouse;

import java.util.List;
import java.util.Optional;

/** 仓库仓储接口（定义在领域层，实现在基础设施层）。 */
public interface WarehouseRepository {

    Optional<Warehouse> findById(Long id);

    Optional<Warehouse> findByCode(Long tenantId, String whCode);

    List<Warehouse> findAll(Long tenantId);

    List<Warehouse> findByStatus(Long tenantId, Integer status);

    void save(Warehouse warehouse);

    void update(Warehouse warehouse);

    void deleteById(Long id);

    boolean existsByCode(Long tenantId, String whCode, Long excludeId);
}
