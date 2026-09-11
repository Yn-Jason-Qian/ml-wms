package com.wms.inventory.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.context.UserContext;
import com.wms.common.exception.BusinessException;
import com.wms.common.util.DocNoUtil;
import com.wms.inventory.application.assembler.StocktakeAssembler;
import com.wms.inventory.application.dto.CountSubmitCmd;
import com.wms.inventory.application.dto.StocktakeCreateCmd;
import com.wms.inventory.application.dto.StocktakeDTO;
import com.wms.inventory.application.dto.StocktakePageQuery;
import com.wms.inventory.domain.entity.Stock;
import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.domain.entity.StocktakeLine;
import com.wms.inventory.domain.gateway.MasterDataGateway;
import com.wms.inventory.domain.repository.StockRepository;
import com.wms.inventory.domain.repository.StocktakeRepository;
import com.wms.inventory.infrastructure.mapper.StocktakeHeaderMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StocktakeAppService {
    private final StocktakeRepository stocktakeRepository;
    private final StocktakeHeaderMapper stocktakeMapper;
    private final StockRepository stockRepository;
    private final MasterDataGateway masterDataGateway;
    private final StocktakeAssembler assembler;

    public IPage<StocktakeDTO> pageStocktake(StocktakePageQuery query) {
        IPage<StocktakeHeader> result =
                stocktakeMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<StocktakeHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        StocktakeHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        StocktakeHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(StocktakeHeader::getCreatedAt));
        return result.convert(assembler::toDTO);
    }

    @Transactional
    public StocktakeDTO createStocktake(StocktakeCreateCmd cmd) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        String no = DocNoUtil.next("ST");

        StocktakeHeader h = new StocktakeHeader();
        h.setTenantId(tenantId);
        h.setWarehouseId(cmd.getWarehouseId());
        h.setStocktakeNo(no);
        h.setStocktakeType(cmd.getStocktakeType());
        h.setStocktakeMode(cmd.getStocktakeMode());
        h.setLocationRange(cmd.getLocationRange());
        h.setPlanStartTime(cmd.getPlanStartTime());
        h.setPlanEndTime(cmd.getPlanEndTime());
        h.setStatus(StocktakeHeader.Status.CREATED);
        h.setTotalLines(0);
        h.setCreatedBy(userId);
        h.setUpdatedBy(userId);
        stocktakeRepository.saveHeader(h);

        // 按当前库存快照生成盘点明细行
        List<StocktakeLine> lines = generateLines(h, tenantId, userId);
        h.setTotalLines(lines.size());
        h.setFirstCountLines(0);
        h.setSecondCountLines(0);
        h.setDiffLines(0);
        stocktakeRepository.updateHeader(h);
        return assembler.toDTO(h, lines);
    }

    public StocktakeDTO getStocktake(Long id) {
        StocktakeHeader h =
                stocktakeRepository
                        .findHeaderById(id)
                        .orElseThrow(() -> BusinessException.notFound("盘点单不存在"));
        return assembler.toDTO(h, stocktakeRepository.findLinesByHeader(id));
    }

    /** PDA 提交盘点结果：写回实盘数量、计算差异，并按进度推进单据状态。 */
    @Transactional
    public void submitCount(CountSubmitCmd cmd) {
        Long userId = UserContext.getUserId();
        StocktakeLine line =
                stocktakeRepository
                        .findLineById(cmd.getLineId())
                        .orElseThrow(() -> BusinessException.notFound("盘点行不存在"));
        StocktakeHeader h =
                stocktakeRepository
                        .findHeaderById(line.getStocktakeHeaderId())
                        .orElseThrow(() -> BusinessException.notFound("盘点单不存在"));
        if (h.getStatus() == StocktakeHeader.Status.DONE
                || h.getStatus() == StocktakeHeader.Status.CANCELLED) {
            throw BusinessException.conflict("盘点单已结束，不可重复提交");
        }

        BigDecimal bookQty = line.getBookQty() != null ? line.getBookQty() : BigDecimal.ZERO;
        BigDecimal diffQty = cmd.getCountQty().subtract(bookQty);
        if (cmd.getCountRound() != null && cmd.getCountRound() == 2) {
            line.setSecondCountQty(cmd.getCountQty());
            line.setSecondCountBy(userId);
            line.setSecondCountAt(LocalDateTime.now());
        } else {
            line.setFirstCountQty(cmd.getCountQty());
            line.setFirstCountBy(userId);
            line.setFirstCountAt(LocalDateTime.now());
        }
        line.setDiffQty(diffQty);
        line.setStatus(diffQty.compareTo(BigDecimal.ZERO) == 0 ? "COUNTED" : "DIFF");
        line.setUpdatedBy(userId);
        stocktakeRepository.updateLine(line);

        if (h.getStatus() == StocktakeHeader.Status.CREATED) {
            h.start();
        }

        List<StocktakeLine> lines = stocktakeRepository.findLinesByHeader(h.getId());
        int diffLines = (int) lines.stream().filter(l -> "DIFF".equals(l.getStatus())).count();
        h.setTotalLines(lines.size());
        h.setFirstCountLines(
                (int) lines.stream().filter(l -> l.getFirstCountQty() != null).count());
        h.setSecondCountLines(
                (int) lines.stream().filter(l -> l.getSecondCountQty() != null).count());
        h.setDiffLines(diffLines);
        boolean allCounted =
                !lines.isEmpty() && lines.stream().allMatch(l -> l.getFirstCountQty() != null);
        if (allCounted) {
            h.finish();
            if (diffLines > 0) {
                h.reviewDiffs();
            }
        }
        h.setUpdatedBy(userId);
        stocktakeRepository.updateHeader(h);
    }

    /** 按仓库现有库存快照生成盘点行（账面数量 = 当前在手库存）。 */
    private List<StocktakeLine> generateLines(StocktakeHeader h, Long tenantId, Long userId) {
        List<Stock> stocks = stockRepository.findByWarehouse(tenantId, h.getWarehouseId());
        Map<Long, String> locationCodes =
                masterDataGateway.resolveLocationCodes(
                        stocks.stream().map(Stock::getLocationId).toList());

        List<StocktakeLine> lines = new ArrayList<>();
        int lineNo = 0;
        for (Stock stock : stocks) {
            if (stock.getQtyOnHand() == null
                    || stock.getQtyOnHand().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            lineNo++;
            StocktakeLine line = new StocktakeLine();
            line.setTenantId(tenantId);
            line.setStocktakeHeaderId(h.getId());
            line.setLineNo(lineNo);
            line.setLocationId(stock.getLocationId());
            line.setLocationCode(locationCodes.get(stock.getLocationId()));
            line.setSkuId(stock.getSkuId());
            line.setSkuCode(stock.getSkuCode());
            line.setSkuName(stock.getSkuName());
            line.setBatchNo(stock.getBatchNo());
            line.setBookQty(stock.getQtyOnHand());
            line.setDiffQty(BigDecimal.ZERO);
            line.setStatus("CREATED");
            line.setCreatedBy(userId);
            line.setUpdatedBy(userId);
            stocktakeRepository.saveLine(line);
            lines.add(line);
        }
        return lines;
    }
}
