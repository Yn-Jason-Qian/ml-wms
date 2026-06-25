package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.inventory.application.assembler.FreezeAssembler;
import com.wms.inventory.application.dto.FreezeCreateCmd;
import com.wms.inventory.application.dto.FreezeDTO;
import com.wms.inventory.application.dto.FreezePageQuery;
import com.wms.inventory.domain.entity.Freeze;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.repository.FreezeRepository;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.service.StockDomainService;
import com.wms.inventory.infrastructure.mapper.FreezeMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FreezeAppService {
    private final FreezeRepository freezeRepository;
    private final FreezeMapper freezeMapper;
    private final StockRepository stockRepository;
    private final StockDomainService stockDomainService;
    private final FreezeAssembler assembler;

    public IPage<FreezeDTO> pageFreeze(FreezePageQuery query) {
        IPage<Freeze> result =
                freezeMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<Freeze>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        Freeze::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(query.getStatus() != null, Freeze::getStatus, query.getStatus())
                                .orderByDesc(Freeze::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

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
}
