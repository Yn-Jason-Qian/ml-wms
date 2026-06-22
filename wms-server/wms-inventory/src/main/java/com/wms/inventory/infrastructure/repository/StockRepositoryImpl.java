package com.wms.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.infrastructure.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {
    private final StockMapper mapper;

    @Override public Optional<Stock> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override
    public Optional<Stock> findByKey(Long tenantId, Long warehouseId, Long locationId, Long skuId, String batchNo) {
        return Optional.ofNullable(mapper.findByKey(tenantId, warehouseId, locationId, skuId, batchNo));
    }

    @Override
    public List<Stock> findByLocation(Long tenantId, Long locationId) {
        return mapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getTenantId, tenantId).eq(Stock::getLocationId, locationId));
    }

    @Override
    public List<Stock> findBySku(Long tenantId, Long skuId) {
        return mapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getTenantId, tenantId).eq(Stock::getSkuId, skuId));
    }

    @Override public void save(Stock stock) { mapper.insert(stock); }
    @Override public void update(Stock stock) { mapper.updateById(stock); }
    @Override public int updateWithVersion(Stock stock) { return mapper.updateWithVersion(stock); }
}
