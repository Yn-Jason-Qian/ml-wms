package com.wms.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inventory.domain.entity.Freeze;
import com.wms.inventory.domain.repository.FreezeRepository;
import com.wms.inventory.infrastructure.mapper.FreezeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FreezeRepositoryImpl implements FreezeRepository {
    private final FreezeMapper mapper;

    @Override public Optional<Freeze> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override
    public List<Freeze> findByStatus(Long tenantId, String status) {
        return mapper.selectList(new LambdaQueryWrapper<Freeze>()
                .eq(Freeze::getTenantId, tenantId).eq(Freeze::getStatus, status));
    }

    @Override
    public List<Freeze> findBySku(Long tenantId, Long skuId) {
        return mapper.selectList(new LambdaQueryWrapper<Freeze>()
                .eq(Freeze::getTenantId, tenantId).eq(Freeze::getSkuId, skuId));
    }

    @Override public void save(Freeze freeze) { mapper.insert(freeze); }
    @Override public void update(Freeze freeze) { mapper.updateById(freeze); }
}
