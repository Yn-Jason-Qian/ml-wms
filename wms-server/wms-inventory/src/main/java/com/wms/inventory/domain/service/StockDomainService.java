package com.wms.inventory.domain.service;

import com.wms.inventory.domain.entity.Freeze;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StockTransaction;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.repository.StockTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockDomainService {
    private final StockRepository stockRepository;
    private final StockTransactionRepository txnRepository;

    /** 库存移动：来源扣减 → 目标增加 → 两条流水 */
    public Stock moveStock(
            Stock fromStock, Stock toStock, BigDecimal qty, Long refId, String refNo, Long moveBy) {
        // 来源扣减
        fromStock.deduct(qty);
        stockRepository.updateWithVersion(fromStock);
        writeTxn(
                fromStock, "MOVE", "OUT", qty, refNo, refId, null, toStock.getLocationId(), moveBy);

        // 目标增加（若不存在则创建）
        if (toStock == null || toStock.getId() == null) {
            toStock = new Stock();
            toStock.setWarehouseId(fromStock.getWarehouseId());
            toStock.setOwnerId(fromStock.getOwnerId());
            toStock.setSkuId(fromStock.getSkuId());
            toStock.setSkuCode(fromStock.getSkuCode());
            toStock.setSkuName(fromStock.getSkuName());
            toStock.setBatchNo(fromStock.getBatchNo());
            toStock.setLotAttrs(fromStock.getLotAttrs());
            toStock.setProductionDate(fromStock.getProductionDate());
            toStock.setExpiryDate(fromStock.getExpiryDate());
            toStock.setTenantId(fromStock.getTenantId());
            toStock.setCreatedBy(moveBy);
            toStock.setUpdatedBy(moveBy);
        }
        toStock.add(qty);
        if (toStock.getId() == null) {
            stockRepository.save(toStock);
        } else {
            stockRepository.updateWithVersion(toStock);
        }
        writeTxn(
                toStock,
                "MOVE",
                "IN",
                qty,
                refNo,
                refId,
                fromStock.getLocationId(),
                toStock.getLocationId(),
                moveBy);

        return toStock;
    }

    /** 冻结库存 */
    public void freezeStock(Stock stock, Freeze freeze, Long userId) {
        stock.freeze(freeze.getFreezeQty());
        stockRepository.updateWithVersion(stock);
        writeTxn(
                stock,
                "FREEZE",
                "OUT",
                freeze.getFreezeQty(),
                freeze.getId().toString(),
                freeze.getId(),
                null,
                null,
                userId);
    }

    /** 解冻库存 */
    public void unfreezeStock(Stock stock, Freeze freeze, Long userId) {
        stock.unfreeze(freeze.getFreezeQty());
        stockRepository.updateWithVersion(stock);
        writeTxn(
                stock,
                "UNFREEZE",
                "IN",
                freeze.getFreezeQty(),
                freeze.getId().toString(),
                freeze.getId(),
                null,
                null,
                userId);
    }

    /** 盘点调整库存 */
    public void adjustStock(
            Stock stock, BigDecimal diffQty, Long refId, String refNo, Long userId) {
        if (diffQty.compareTo(BigDecimal.ZERO) > 0) {
            stock.add(diffQty);
            stockRepository.updateWithVersion(stock);
            writeTxn(stock, "ADJUST", "IN", diffQty, refNo, refId, null, null, userId);
        } else if (diffQty.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal absQty = diffQty.abs();
            stock.deduct(absQty);
            stockRepository.updateWithVersion(stock);
            writeTxn(stock, "ADJUST", "OUT", absQty, refNo, refId, null, null, userId);
        }
    }

    private void writeTxn(
            Stock stock,
            String txnType,
            String direction,
            BigDecimal qty,
            String refNo,
            Long refId,
            Long fromLoc,
            Long toLoc,
            Long operator) {
        StockTransaction txn = new StockTransaction();
        txn.setWarehouseId(stock.getWarehouseId());
        txn.setTenantId(stock.getTenantId());
        txn.setStockId(stock.getId());
        txn.setSkuId(stock.getSkuId());
        txn.setSkuCode(stock.getSkuCode());
        txn.setBatchNo(stock.getBatchNo());
        txn.setTxnType(txnType);
        txn.setTxnDirection(direction);
        txn.setTxnQty(qty);
        txn.setQtyBefore(stock.getQtyOnHand());
        txn.setQtyAfter(stock.getQtyOnHand().add(direction.equals("IN") ? qty : qty.negate()));
        txn.setRefNo(refNo);
        txn.setRefId(refId);
        txn.setFromLocationId(fromLoc);
        txn.setToLocationId(toLoc);
        txn.setTxnTime(LocalDateTime.now());
        txn.setCreatedBy(operator);
        txn.setUpdatedBy(operator);
        txnRepository.save(txn);
    }
}
