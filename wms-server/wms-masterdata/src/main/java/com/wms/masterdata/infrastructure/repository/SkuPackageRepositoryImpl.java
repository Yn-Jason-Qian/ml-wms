package com.wms.masterdata.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.masterdata.domain.entity.SkuPackage;
import com.wms.masterdata.domain.repository.SkuPackageRepository;
import com.wms.masterdata.infrastructure.mapper.SkuPackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SkuPackageRepositoryImpl implements SkuPackageRepository {
    private final SkuPackageMapper mapper;

    @Override
    public Optional<SkuPackage> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override
    public List<SkuPackage> findBySkuId(Long tenantId, Long skuId) {
        return mapper.selectList(new LambdaQueryWrapper<SkuPackage>()
                .eq(SkuPackage::getTenantId, tenantId).eq(SkuPackage::getSkuId, skuId));
    }

    @Override public void save(SkuPackage pkg) { mapper.insert(pkg); }
    @Override public void update(SkuPackage pkg) { mapper.updateById(pkg); }
    @Override public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByLevel(Long tenantId, Long skuId, String packageLevel, Long excludeId) {
        return mapper.existsByLevel(tenantId, skuId, packageLevel, excludeId);
    }
}
