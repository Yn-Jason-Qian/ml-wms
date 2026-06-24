package com.wms.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inventory.domain.entity.StockTransaction;
import com.wms.inventory.domain.repository.StockTransactionRepository;
import com.wms.inventory.infrastructure.mapper.StockTransactionMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockTransactionRepositoryImpl implements StockTransactionRepository {
    private final StockTransactionMapper mapper;

    @Override
    public void save(StockTransaction txn) {
        mapper.insert(txn);
    }

    @Override
    public List<StockTransaction> findByStockId(Long tenantId, Long stockId) {
        return mapper.selectList(
                new LambdaQueryWrapper<StockTransaction>()
                        .eq(StockTransaction::getTenantId, tenantId)
                        .eq(StockTransaction::getStockId, stockId)
                        .orderByDesc(StockTransaction::getTxnTime));
    }

    @Override
    public List<StockTransaction> findByRefId(Long tenantId, Long refId) {
        return mapper.selectList(
                new LambdaQueryWrapper<StockTransaction>()
                        .eq(StockTransaction::getTenantId, tenantId)
                        .eq(StockTransaction::getRefId, refId));
    }
}
