package com.wms.inventory.domain.repository;

import com.wms.inventory.domain.entity.StockTransaction;

import java.util.List;

public interface StockTransactionRepository {
    void save(StockTransaction txn);

    List<StockTransaction> findByStockId(Long tenantId, Long stockId);

    List<StockTransaction> findByRefId(Long tenantId, Long refId);
}
