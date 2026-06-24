package com.wms.outbound.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StockTransaction;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.repository.StockTransactionRepository;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.domain.repository.OutboundRepository;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.repository.StrategyRepository;
import com.wms.strategy.domain.service.StrategyEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundDomainService {
    private final OutboundRepository outboundRepo;
    private final StockRepository stockRepository;
    private final StockTransactionRepository txnRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyEngine strategyEngine;

    /** 库存分配（FIFO 优先） */
    public void allocateInventory(OrderLine orderLine, Long tenantId, Long warehouseId) {
        // 用策略引擎匹配分配策略
        Map<String, Object> context = new HashMap<>();
        context.put("order", Map.of("priority", "5"));
        context.put(
                "sku", Map.of("code", orderLine.getSkuCode(), "batchNo", orderLine.getBatchNo()));
        StrategyEngine.MatchResult result =
                strategyEngine.match(
                        StrategyConfig.TYPE_ALLOCATION,
                        context,
                        type -> loadConfigs(tenantId, type));

        // 默认 FIFO：按入库时间升序找库存
        List<Stock> stocks = stockRepository.findBySku(tenantId, orderLine.getSkuId());
        stocks.sort(
                Comparator.comparing(
                        Stock::getFirstInTime, Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal remaining = orderLine.getRemainingQty();
        for (Stock stock : stocks) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal allocatable = stock.getQtyAvailable().min(remaining);
            if (allocatable.compareTo(BigDecimal.ZERO) <= 0) continue;

            stock.allocate(allocatable);
            stockRepository.updateWithVersion(stock);
            writeTxn(
                    stock,
                    "ALLOCATE",
                    "OUT",
                    allocatable,
                    orderLine.getOrderHeaderId().toString(),
                    orderLine.getOrderHeaderId(),
                    null);

            remaining = remaining.subtract(allocatable);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw BusinessException.conflict(
                    "库存不足，SKU [" + orderLine.getSkuCode() + "] 缺 " + remaining + " 件");
        }
    }

    /** 发货确认：扣除库存 + 写流水 */
    public void shipDeduct(PickLine pickLine, Long userId) {
        Stock stock =
                stockRepository
                        .findByKey(
                                pickLine.getTenantId(),
                                null,
                                pickLine.getLocationId(),
                                pickLine.getSkuId(),
                                pickLine.getBatchNo())
                        .orElseThrow(() -> BusinessException.notFound("库存不存在"));

        stock.deduct(pickLine.getPickedQty());
        stockRepository.updateWithVersion(stock);
        writeTxn(
                stock,
                "SHIP",
                "OUT",
                pickLine.getPickedQty(),
                pickLine.getPickHeaderId().toString(),
                pickLine.getPickHeaderId(),
                userId);
    }

    private List<StrategyConfig> loadConfigs(Long tenantId, String type) {
        List<StrategyConfig> configs = strategyRepository.findByType(tenantId, type);
        configs.forEach(c -> c.setRules(strategyRepository.findRulesByStrategy(c.getId())));
        return configs;
    }

    private void writeTxn(
            Stock stock,
            String type,
            String direction,
            BigDecimal qty,
            String refNo,
            Long refId,
            Long operator) {
        StockTransaction txn = new StockTransaction();
        txn.setWarehouseId(stock.getWarehouseId());
        txn.setTenantId(stock.getTenantId());
        txn.setStockId(stock.getId());
        txn.setSkuId(stock.getSkuId());
        txn.setSkuCode(stock.getSkuCode());
        txn.setBatchNo(stock.getBatchNo());
        txn.setTxnType(type);
        txn.setTxnDirection(direction);
        txn.setTxnQty(qty);
        txn.setQtyBefore(stock.getQtyOnHand());
        txn.setQtyAfter(
                direction.equals("OUT")
                        ? stock.getQtyOnHand().subtract(qty)
                        : stock.getQtyOnHand().add(qty));
        txn.setRefNo(refNo);
        txn.setRefId(refId);
        txn.setTxnTime(LocalDateTime.now());
        txn.setCreatedBy(operator);
        txn.setUpdatedBy(operator);
        txnRepository.save(txn);
    }
}
