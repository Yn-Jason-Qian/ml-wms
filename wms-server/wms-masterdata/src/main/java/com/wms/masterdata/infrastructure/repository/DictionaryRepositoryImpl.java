package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.Dictionary;
import com.wms.masterdata.domain.repository.DictionaryRepository;
import com.wms.masterdata.infrastructure.mapper.DictionaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DictionaryRepositoryImpl implements DictionaryRepository {
    private final DictionaryMapper mapper;

    @Override
    public Optional<Dictionary> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override
    public List<Dictionary> findByType(Long tenantId, String dictType) {
        return mapper.selectList(new LambdaQueryWrapper<Dictionary>()
                .eq(Dictionary::getTenantId, tenantId)
                .eq(Dictionary::getDictType, dictType)
                .orderByAsc(Dictionary::getSortOrder));
    }

    @Override
    public List<String> findAllTypes(Long tenantId) {
        return mapper.findAllTypes(tenantId);
    }

    @Override public void save(Dictionary dict) { mapper.insert(dict); }
    @Override public void update(Dictionary dict) { mapper.updateById(dict); }
    @Override public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByCode(Long tenantId, String dictType, String dictCode, Long excludeId) {
        return mapper.existsByCode(tenantId, dictType, dictCode, excludeId);
    }
}
