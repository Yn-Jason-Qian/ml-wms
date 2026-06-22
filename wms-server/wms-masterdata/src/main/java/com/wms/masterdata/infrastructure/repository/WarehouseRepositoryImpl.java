package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Warehouse;
import com.wms.masterdata.domain.repository.WarehouseRepository;
import com.wms.masterdata.infrastructure.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final WarehouseMapper mapper;

    @Override
    public Optional<Warehouse> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<Warehouse> findByCode(Long tenantId, String whCode) {
        Warehouse w = mapper.selectOne(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getWhCode, whCode));
        return Optional.ofNullable(w);
    }

    @Override
    public List<Warehouse> findAll(Long tenantId) {
        return mapper.selectList(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId));
    }

    @Override
    public List<Warehouse> findByStatus(Long tenantId, Integer status) {
        return mapper.selectList(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getStatus, status));
    }

    @Override
    public void save(Warehouse warehouse) {
        mapper.insert(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
        mapper.updateById(warehouse);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(Long tenantId, String whCode, Long excludeId) {
        return mapper.existsByCode(tenantId, whCode, excludeId);
    }
}
