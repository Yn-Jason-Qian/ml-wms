package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Location;
import com.wms.masterdata.domain.repository.LocationRepository;
import com.wms.masterdata.infrastructure.mapper.LocationMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {
    private final LocationMapper mapper;

    @Override
    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<Location> findByArea(Long tenantId, Long areaId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getTenantId, tenantId)
                        .eq(Location::getAreaId, areaId));
    }

    @Override
    public List<Location> findByWarehouse(Long tenantId, Long warehouseId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getTenantId, tenantId)
                        .eq(Location::getWarehouseId, warehouseId));
    }

    @Override
    public void save(Location location) {
        mapper.insert(location);
    }

    @Override
    public void batchSave(List<Location> locations) {
        locations.forEach(mapper::insert);
    }

    @Override
    public void update(Location location) {
        mapper.updateById(location);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(
            Long tenantId, Long warehouseId, String locationCode, Long excludeId) {
        return mapper.existsByCode(tenantId, warehouseId, locationCode, excludeId);
    }
}
