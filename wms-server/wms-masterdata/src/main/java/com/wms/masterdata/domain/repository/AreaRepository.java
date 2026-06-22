package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Area;
import java.util.List;
import java.util.Optional;

public interface AreaRepository {
    Optional<Area> findById(Long id);
    List<Area> findByWarehouse(Long tenantId, Long warehouseId);
    List<Area> findAll(Long tenantId);
    void save(Area area);
    void update(Area area);
    void deleteById(Long id);
    boolean existsByCode(Long tenantId, Long warehouseId, String areaCode, Long excludeId);
}
