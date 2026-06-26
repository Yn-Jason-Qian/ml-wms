package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inventory.application.assembler.StockAssembler;
import com.wms.inventory.application.dto.StockDTO;
import com.wms.inventory.application.dto.StockQuery;
import com.wms.inventory.application.dto.StockTransactionDTO;
import com.wms.inventory.application.dto.TransactionQuery;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StockTransaction;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.repository.StockTransactionRepository;
import com.wms.inventory.domain.service.StockDomainService;
import com.wms.inventory.infrastructure.mapper.StockMapper;
import com.wms.inventory.infrastructure.mapper.StockTransactionMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockAppService {
    private final StockRepository stockRepository;
    private final StockTransactionRepository txnRepository;
    private final StockDomainService stockDomainService;
    private final StockAssembler assembler;

    // Mappers — 仅用于分页查询
    private final StockMapper stockMapper;
    private final StockTransactionMapper txnMapper;

    // ───── 库存查询 ─────

    public IPage<StockDTO> pageStock(StockQuery query) {
        Long tenantId = UserContext.getTenantId();
        Page<Stock> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Stock> qw =
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getTenantId, tenantId)
                        .eq(
                                query.getWarehouseId() != null,
                                Stock::getWarehouseId,
                                query.getWarehouseId())
                        .eq(query.getOwnerId() != null, Stock::getOwnerId, query.getOwnerId())
                        .eq(
                                query.getLocationId() != null,
                                Stock::getLocationId,
                                query.getLocationId())
                        .eq(query.getSkuId() != null, Stock::getSkuId, query.getSkuId())
                        .like(query.getSkuCode() != null, Stock::getSkuCode, query.getSkuCode())
                        .like(query.getSkuName() != null, Stock::getSkuName, query.getSkuName())
                        .eq(query.getBatchNo() != null, Stock::getBatchNo, query.getBatchNo())
                        .eq(query.getStatus() != null, Stock::getStatus, query.getStatus());
        if (Boolean.TRUE.equals(query.getOnlyAvailable())) {
            qw.gt(Stock::getQtyAvailable, BigDecimal.ZERO);
        }
        if (query.getExpiryWithinDays() != null) {
            qw.le(Stock::getExpiryDate, LocalDateTime.now().plusDays(query.getExpiryWithinDays()));
            qw.gt(Stock::getQtyOnHand, BigDecimal.ZERO);
        }
        IPage<Stock> result = stockMapper.selectPage(page, qw);
        return result.convert(assembler::toDTO);
    }

    public StockDTO findStockById(Long id) {
        return assembler.toDTO(
                stockRepository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.notFound("库存记录不存在")));
    }

    // ───── 跨域调用（供其他模块 Gateway Adapter 使用）─────

    public void increaseStock(
            Long tenantId,
            Long warehouseId,
            Long ownerId,
            Long locationId,
            Long skuId,
            String skuCode,
            String skuName,
            String batchNo,
            String lotAttrs,
            BigDecimal qty,
            StockTransaction.TxnType txnType,
            String refNo,
            Long refId,
            Long userId) {
        stockDomainService.increaseStock(
                tenantId,
                warehouseId,
                ownerId,
                locationId,
                skuId,
                skuCode,
                skuName,
                batchNo,
                lotAttrs,
                qty,
                txnType,
                refNo,
                refId,
                userId);
    }

    public void allocateBySku(
            Long tenantId, Long skuId, BigDecimal requiredQty, String refNo, Long refId) {
        stockDomainService.allocateBySku(tenantId, skuId, requiredQty, refNo, refId);
    }

    /** 查询 SKU 的首选库位（有可用库存的库位，FIFO 优先）。 */
    public Long findLocationBySku(Long tenantId, Long skuId, String batchNo) {
        List<Stock> stocks = stockRepository.findBySku(tenantId, skuId);
        stocks.sort(
                java.util.Comparator.comparing(
                        Stock::getFirstInTime,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        for (Stock stock : stocks) {
            if (stock.getQtyAvailable().compareTo(BigDecimal.ZERO) <= 0) continue;
            if (batchNo != null && !batchNo.isBlank() && !batchNo.equals(stock.getBatchNo()))
                continue;
            return stock.getLocationId();
        }
        // 无可用库存时，返回第一个库存的库位
        if (!stocks.isEmpty()) {
            return stocks.get(0).getLocationId();
        }
        return null;
    }

    public void deductStock(
            Long tenantId,
            Long warehouseId,
            Long locationId,
            Long skuId,
            String batchNo,
            BigDecimal qty,
            StockTransaction.TxnType txnType,
            String refNo,
            Long refId,
            Long userId) {
        stockDomainService.deductStock(
                tenantId,
                warehouseId,
                locationId,
                skuId,
                batchNo,
                qty,
                txnType,
                refNo,
                refId,
                userId);
    }

    // ───── 库存流水 ─────

    public List<StockTransactionDTO> findTransactionsByStock(Long stockId) {
        return txnRepository.findByStockId(UserContext.getTenantId(), stockId).stream()
                .map(assembler::toDTO)
                .collect(Collectors.toList());
    }

    public IPage<StockTransactionDTO> pageTransactions(TransactionQuery query) {
        Page<StockTransaction> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<StockTransaction> result =
                txnMapper.selectPage(
                        page,
                        new LambdaQueryWrapper<StockTransaction>()
                                .eq(StockTransaction::getTenantId, UserContext.getTenantId())
                                .eq(
                                        query.getStockId() != null,
                                        StockTransaction::getStockId,
                                        query.getStockId())
                                .eq(
                                        query.getSkuId() != null,
                                        StockTransaction::getSkuId,
                                        query.getSkuId())
                                .eq(
                                        query.getRefId() != null,
                                        StockTransaction::getRefId,
                                        query.getRefId())
                                .eq(
                                        query.getTxnType() != null,
                                        StockTransaction::getTxnType,
                                        query.getTxnType())
                                .orderByDesc(StockTransaction::getTxnTime));
        return result.convert(assembler::toDTO);
    }
}
