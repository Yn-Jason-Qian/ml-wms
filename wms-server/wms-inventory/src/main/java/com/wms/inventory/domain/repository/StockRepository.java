package com.wms.inventory.domain.repository;

import com.wms.inventory.domain.entity.Stock;
import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findById(Long id);
    Optional<Stock> findByKey(Long tenantId, Long warehouseId, Long locationId, Long skuId, String batchNo);
    List<Stock> findByLocation(Long tenantId, Long locationId);
    List<Stock> findBySku(Long tenantId, Long skuId);
    void save(Stock stock);
    void update(Stock stock);
    /** 乐观锁更新，返回受影响行数 */
    int updateWithVersion(Stock stock);
}
