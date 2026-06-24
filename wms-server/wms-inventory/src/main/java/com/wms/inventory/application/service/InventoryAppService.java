package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inventory.application.assembler.InventoryAssembler;
import com.wms.inventory.application.dto.*;
import com.wms.inventory.domain.entity.*;
import com.wms.inventory.domain.repository.*;
import com.wms.inventory.domain.service.StockDomainService;
import com.wms.inventory.infrastructure.mapper.StockMapper;
import com.wms.inventory.infrastructure.mapper.StockTransactionMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryAppService {
    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final StockTransactionRepository txnRepository;
    private final StockTransactionMapper txnMapper;
    private final MoveRepository moveRepository;
    private final FreezeRepository freezeRepository;
    private final StocktakeRepository stocktakeRepository;
    private final StockDomainService stockDomainService;
    private final InventoryAssembler assembler;

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

    // ───── 库存移动 ─────

    @Transactional
    public MoveDTO createMove(MoveCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String moveNo =
                "MV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 创建移库单头
        MoveHeader header = new MoveHeader();
        header.setTenantId(tenantId);
        header.setWarehouseId(cmd.getWarehouseId());
        header.setMoveNo(moveNo);
        header.setMoveType(cmd.getMoveType());
        header.setStatus(MoveHeader.STATUS_CREATED);
        header.setRemark(cmd.getRemark());
        header.setCreatedBy(userId);
        header.setUpdatedBy(userId);
        moveRepository.saveHeader(header);

        // 创建移库单行 + 执行移动
        Stock fromStock =
                stockRepository
                        .findByKey(
                                tenantId,
                                cmd.getWarehouseId(),
                                cmd.getFromLocationId(),
                                cmd.getSkuId(),
                                cmd.getBatchNo())
                        .orElseThrow(() -> BusinessException.notFound("来源库位库存不存在或不足"));
        Stock toStock =
                stockRepository
                        .findByKey(
                                tenantId,
                                cmd.getWarehouseId(),
                                cmd.getToLocationId(),
                                cmd.getSkuId(),
                                cmd.getBatchNo())
                        .orElse(null);

        stockDomainService.moveStock(
                fromStock, toStock, cmd.getMoveQty(), header.getId(), moveNo, userId);

        MoveLine line = new MoveLine();
        line.setTenantId(tenantId);
        line.setMoveHeaderId(header.getId());
        line.setLineNo(1);
        line.setSkuId(cmd.getSkuId());
        line.setSkuCode(fromStock.getSkuCode());
        line.setSkuName(fromStock.getSkuName());
        line.setMoveQty(cmd.getMoveQty());
        line.setFromLocationId(cmd.getFromLocationId());
        line.setFromStockId(fromStock.getId());
        line.setToLocationId(cmd.getToLocationId());
        line.setBatchNo(cmd.getBatchNo());
        line.setStatus("DONE");
        line.setMoveBy(userId);
        line.setMoveAt(LocalDateTime.now());
        line.setCreatedBy(userId);
        line.setUpdatedBy(userId);
        moveRepository.saveLine(line);

        header.setStatus(MoveHeader.STATUS_DONE);
        moveRepository.updateHeader(header);

        return assembler.toDTO(header, List.of(line));
    }

    // ───── 库存冻结 ─────

    @Transactional
    public FreezeDTO createFreeze(FreezeCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();

        Stock stock =
                stockRepository
                        .findById(cmd.getStockId())
                        .orElseThrow(() -> BusinessException.notFound("库存记录不存在"));

        Freeze freeze = new Freeze();
        freeze.setTenantId(tenantId);
        freeze.setWarehouseId(cmd.getWarehouseId());
        freeze.setFreezeType(cmd.getFreezeType());
        freeze.setStockId(cmd.getStockId());
        freeze.setSkuId(cmd.getSkuId());
        freeze.setLocationId(cmd.getLocationId());
        freeze.setBatchNo(cmd.getBatchNo());
        freeze.setFreezeQty(cmd.getFreezeQty());
        freeze.setReason(cmd.getReason());
        freeze.setStatus(Freeze.STATUS_ACTIVE);
        freeze.setFreezeBy(userId);
        freeze.setFreezeAt(LocalDateTime.now());
        freeze.setCreatedBy(userId);
        freeze.setUpdatedBy(userId);
        freezeRepository.save(freeze);

        stockDomainService.freezeStock(stock, freeze, userId);
        return assembler.toDTO(freeze);
    }

    @Transactional
    public void releaseFreeze(Long freezeId) {
        Long userId = UserContext.getUserId();
        Freeze freeze =
                freezeRepository
                        .findById(freezeId)
                        .orElseThrow(() -> BusinessException.notFound("冻结记录不存在"));
        if (!Freeze.STATUS_ACTIVE.equals(freeze.getStatus())) {
            throw new BusinessException("冻结记录已释放");
        }

        Stock stock =
                stockRepository
                        .findById(freeze.getStockId())
                        .orElseThrow(() -> BusinessException.notFound("库存记录不存在"));

        freeze.release();
        freeze.setReleaseBy(userId);
        freeze.setUpdatedBy(userId);
        freezeRepository.update(freeze);

        stockDomainService.unfreezeStock(stock, freeze, userId);
    }

    // ───── 盘点 ─────

    @Transactional
    public StocktakeDTO createStocktake(StocktakeCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String no =
                "ST-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        StocktakeHeader h = new StocktakeHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setStocktakeNo(no);
        h.setStocktakeType(cmd.getStocktakeType());
        h.setStocktakeMode(cmd.getStocktakeMode());
        h.setLocationRange(cmd.getLocationRange());
        h.setPlanStartTime(cmd.getPlanStartTime());
        h.setPlanEndTime(cmd.getPlanEndTime());
        h.setStatus(StocktakeHeader.STATUS_CREATED);
        h.setTotalLines(0);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        stocktakeRepository.saveHeader(h);
        return assembler.toDTO(h, List.of());
    }
}
