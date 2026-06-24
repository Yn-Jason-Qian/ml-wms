package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Sku;
import com.wms.masterdata.domain.repository.SkuRepository;
import com.wms.masterdata.infrastructure.mapper.SkuMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SkuRepositoryImpl implements SkuRepository {
    private final SkuMapper mapper;

    @Override
    public Optional<Sku> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<Sku> findByCode(Long tenantId, String skuCode) {
        return Optional.ofNullable(
                mapper.selectOne(
                        new LambdaQueryWrapper<Sku>()
                                .eq(Sku::getTenantId, tenantId)
                                .eq(Sku::getSkuCode, skuCode)));
    }

    @Override
    public List<Sku> findByOwner(Long tenantId, Long ownerId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getTenantId, tenantId)
                        .eq(Sku::getOwnerId, ownerId));
    }

    @Override
    public List<Sku> findAll(Long tenantId) {
        return mapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getTenantId, tenantId));
    }

    @Override
    public void save(Sku sku) {
        mapper.insert(sku);
    }

    @Override
    public void update(Sku sku) {
        mapper.updateById(sku);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(Long tenantId, String skuCode, Long excludeId) {
        return mapper.existsByCode(tenantId, skuCode, excludeId);
    }
}
