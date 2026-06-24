package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Area;
import com.wms.masterdata.domain.repository.AreaRepository;
import com.wms.masterdata.infrastructure.mapper.AreaMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AreaRepositoryImpl implements AreaRepository {
    private final AreaMapper mapper;

    @Override
    public Optional<Area> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<Area> findByWarehouse(Long tenantId, Long warehouseId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Area>()
                        .eq(Area::getTenantId, tenantId)
                        .eq(Area::getWarehouseId, warehouseId));
    }

    @Override
    public List<Area> findAll(Long tenantId) {
        return mapper.selectList(new LambdaQueryWrapper<Area>().eq(Area::getTenantId, tenantId));
    }

    @Override
    public void save(Area area) {
        mapper.insert(area);
    }

    @Override
    public void update(Area area) {
        mapper.updateById(area);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(Long tenantId, Long warehouseId, String areaCode, Long excludeId) {
        return mapper.existsByCode(tenantId, warehouseId, areaCode, excludeId);
    }
}
