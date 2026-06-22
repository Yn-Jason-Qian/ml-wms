package com.wms.masterdata.domain.repository;

import com.wms.masterdata.domain.entity.Location;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    Optional<Location> findById(Long id);
    List<Location> findByArea(Long tenantId, Long areaId);
    List<Location> findByWarehouse(Long tenantId, Long warehouseId);
    void save(Location location);
    void batchSave(List<Location> locations);
    void update(Location location);
    void deleteById(Long id);
    boolean existsByCode(Long tenantId, Long warehouseId, String locationCode, Long excludeId);
}
