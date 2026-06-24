package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.StocktakeCreateCmd;
import com.wms.inventory.application.dto.StocktakeDTO;
import com.wms.inventory.application.dto.StocktakePageQuery;
import com.wms.inventory.application.service.InventoryAppService;
import com.wms.inventory.domain.entity.StocktakeHeader;
import com.wms.inventory.infrastructure.mapper.StocktakeHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory/stocktakes")
@RequiredArgsConstructor
public class StocktakeController {
    private final InventoryAppService inventoryAppService;
    private final StocktakeHeaderMapper stocktakeHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody StocktakePageQuery query) {
        IPage<StocktakeHeader> result =
                stocktakeHeaderMapper.selectPage(
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
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("stocktakeNo", h.getStocktakeNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("stocktakeType", h.getStocktakeType());
                                            m.put("stocktakeMode", h.getStocktakeMode());
                                            m.put("status", h.getStatus());
                                            m.put("totalLines", h.getTotalLines());
                                            m.put("diffLines", h.getDiffLines());
                                            m.put(
                                                    "createdAt",
                                                    h.getCreatedAt() != null
                                                            ? h.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "库存管理", action = "创建盘点单")
    public ApiResponse<StocktakeDTO> create(@Valid @RequestBody StocktakeCreateCmd cmd) {
        return ApiResponse.ok(inventoryAppService.createStocktake(cmd));
    }
}
