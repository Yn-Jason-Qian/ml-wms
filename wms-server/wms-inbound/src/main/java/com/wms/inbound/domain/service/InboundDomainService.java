package com.wms.inbound.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.inbound.domain.entity.*;
import com.wms.inbound.domain.repository.*;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StockTransaction;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.repository.StockTransactionRepository;
import com.wms.strategy.domain.entity.StrategyConfig;
import com.wms.strategy.domain.repository.StrategyRepository;
import com.wms.strategy.domain.service.StrategyEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundDomainService {
    private final InboundRepository inboundRepo;
    private final StockRepository stockRepository;
    private final StockTransactionRepository txnRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyEngine strategyEngine;

    /** 收货确认：校验数量 + 更新ASN行 + 创建收货行 */
    public AsnLine receiveLine(ReceiveHeader header, ReceiveLine receiveLine, Long userId) {
        // 盲收模式不关联ASN
        if (header.getAsnHeaderId() == null || receiveLine.getAsnLineId() == null) {
            return null;
        }

        AsnLine asnLine =
                inboundRepo
                        .findAsnLineById(receiveLine.getAsnLineId())
                        .orElseThrow(() -> BusinessException.notFound("ASN行不存在"));

        // 校验是否超收
        BigDecimal remaining = asnLine.getRemainingQty();
        if (receiveLine.getReceiveQty().compareTo(remaining) > 0) {
            throw new BusinessException(
                    "收货数量 [" + receiveLine.getReceiveQty() + "] 超过剩余可收数量 [" + remaining + "]");
        }

        // 更新ASN行已收数量
        asnLine.setReceivedQty(asnLine.getReceivedQty().add(receiveLine.getReceiveQty()));
        asnLine.setUpdatedBy(userId);
        inboundRepo.updateAsnLine(asnLine);

        // 更新ASN头状态
        AsnHeader asnHeader = inboundRepo.findAsnHeaderById(header.getAsnHeaderId()).orElse(null);
        if (asnHeader != null) {
            updateAsnStatus(asnHeader, userId);
        }

        return asnLine;
    }

    private void updateAsnStatus(AsnHeader asnHeader, Long userId) {
        List<AsnLine> lines = inboundRepo.findAsnLinesByHeader(asnHeader.getId());
        boolean allReceived =
                lines.stream().allMatch(l -> l.getReceivedQty().compareTo(l.getExpectedQty()) >= 0);
        boolean anyReceived =
                lines.stream().anyMatch(l -> l.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);

        if (allReceived) asnHeader.setStatus(AsnHeader.STATUS_RECEIVED);
        else if (anyReceived) asnHeader.setStatus(AsnHeader.STATUS_PARTIAL_RECEIVED);
        else asnHeader.setStatus(AsnHeader.STATUS_CREATED);
        asnHeader.setUpdatedBy(userId);
        inboundRepo.updateAsnHeader(asnHeader);
    }

    /** 执行上架 + 增加库存 + 写流水 */
    public void executePutaway(PutawayLine line, Long userId) {
        // 调用上架策略引擎获取推荐库位（如果目标库位未指定）
        if (line.getToLocationId() == null) {
            Map<String, Object> context = new HashMap<>();
            context.put("sku", Map.of("code", line.getSkuCode()));
            context.put("putaway", Map.of("fromLocationId", line.getFromLocationId()));
            StrategyEngine.MatchResult result =
                    strategyEngine.match(
                            StrategyConfig.TYPE_PUTAWAY,
                            context,
                            type -> {
                                List<StrategyConfig> configs =
                                        strategyRepository.findByType(line.getTenantId(), type);
                                configs.forEach(
                                        c ->
                                                c.setRules(
                                                        strategyRepository.findRulesByStrategy(
                                                                c.getId())));
                                return configs;
                            });
            if (result != null && result.getActionParams().containsKey("locationId")) {
                line.setToLocationId(
                        Long.valueOf(result.getActionParams().get("locationId").toString()));
            }
        }

        if (line.getToLocationId() == null) {
            throw BusinessException.notFound("未找到合适的上架库位，请手动指定");
        }

        // 查找或创建库存记录
        Long tenantId = line.getTenantId();
        Stock stock =
                stockRepository
                        .findByKey(
                                tenantId,
                                null /*warehouse*/,
                                line.getToLocationId(),
                                line.getSkuId(),
                                line.getBatchNo())
                        .orElse(null);

        if (stock == null) {
            stock = new Stock();
            stock.setTenantId(tenantId);
            stock.setLocationId(line.getToLocationId());
            stock.setSkuId(line.getSkuId());
            stock.setSkuCode(line.getSkuCode());
            stock.setSkuName(line.getSkuName());
            stock.setBatchNo(line.getBatchNo());
            stock.setLotAttrs(line.getLotAttrs());
            stock.setQtyOnHand(BigDecimal.ZERO);
            stock.setQtyAllocated(BigDecimal.ZERO);
            stock.setQtyAvailable(BigDecimal.ZERO);
            stock.setQtyFrozen(BigDecimal.ZERO);
            stock.setCreatedBy(userId);
            stock.setUpdatedBy(userId);
            stockRepository.save(stock);
        }

        stock.add(line.getPutawayQty());
        stockRepository.updateWithVersion(stock);

        // 写库存流水
        writeTransaction(
                stock,
                "PUTAWAY",
                "IN",
                line.getPutawayQty(),
                line.getPutawayHeaderId().toString(),
                line.getPutawayHeaderId(),
                userId);
    }

    private void writeTransaction(
            Stock stock,
            String txnType,
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
        txn.setTxnType(txnType);
        txn.setTxnDirection(direction);
        txn.setTxnQty(qty);
        txn.setQtyBefore(stock.getQtyOnHand().subtract(qty));
        txn.setQtyAfter(stock.getQtyOnHand());
        txn.setRefNo(refNo);
        txn.setRefId(refId);
        txn.setTxnTime(LocalDateTime.now());
        txn.setCreatedBy(operator);
        txn.setUpdatedBy(operator);
        txnRepository.save(txn);
    }
}
