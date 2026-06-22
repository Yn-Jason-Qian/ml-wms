package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Owner;
import com.wms.masterdata.domain.repository.OwnerRepository;
import com.wms.masterdata.infrastructure.mapper.OwnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OwnerRepositoryImpl implements OwnerRepository {
    private final OwnerMapper mapper;

    @Override
    public Optional<Owner> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override
    public List<Owner> findAll(Long tenantId) {
        return mapper.selectList(new LambdaQueryWrapper<Owner>().eq(Owner::getTenantId, tenantId));
    }

    @Override public void save(Owner owner) { mapper.insert(owner); }
    @Override public void update(Owner owner) { mapper.updateById(owner); }
    @Override public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByCode(Long tenantId, String ownerCode, Long excludeId) {
        return mapper.existsByCode(tenantId, ownerCode, excludeId);
    }
}
