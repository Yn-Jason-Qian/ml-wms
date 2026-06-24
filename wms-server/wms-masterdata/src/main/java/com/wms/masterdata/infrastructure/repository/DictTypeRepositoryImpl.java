package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.DictType;
import com.wms.masterdata.domain.repository.DictTypeRepository;
import com.wms.masterdata.infrastructure.mapper.DictTypeMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DictTypeRepositoryImpl implements DictTypeRepository {
    private final DictTypeMapper mapper;

    @Override
    public List<DictType> findAll(Long tenantId) {
        return mapper.selectList(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getTenantId, tenantId)
                        .orderByAsc(DictType::getTypeCode));
    }

    @Override
    public Optional<DictType> findByCode(Long tenantId, String typeCode) {
        return Optional.ofNullable(
                mapper.selectOne(
                        new LambdaQueryWrapper<DictType>()
                                .eq(DictType::getTenantId, tenantId)
                                .eq(DictType::getTypeCode, typeCode)));
    }

    @Override
    public void save(DictType dictType) {
        mapper.insert(dictType);
    }

    @Override
    public void update(DictType dictType) {
        mapper.updateById(dictType);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
