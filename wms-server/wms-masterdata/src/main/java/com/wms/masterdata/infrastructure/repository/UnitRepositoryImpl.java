package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Unit;
import com.wms.masterdata.domain.repository.UnitRepository;
import com.wms.masterdata.infrastructure.mapper.UnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UnitRepositoryImpl implements UnitRepository {
    private final UnitMapper mapper;

    @Override
    public List<Unit> findAll(Long tenantId) {
        return mapper.selectList(new LambdaQueryWrapper<Unit>().eq(Unit::getTenantId, tenantId));
    }
}
