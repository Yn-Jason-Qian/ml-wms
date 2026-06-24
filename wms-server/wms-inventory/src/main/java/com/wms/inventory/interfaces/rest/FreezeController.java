package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.FreezeCreateCmd;
import com.wms.inventory.application.dto.FreezeDTO;
import com.wms.inventory.application.dto.FreezePageQuery;
import com.wms.inventory.application.service.InventoryAppService;
import com.wms.inventory.domain.entity.Freeze;
import com.wms.inventory.infrastructure.mapper.FreezeMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory/freezes")
@RequiredArgsConstructor
public class FreezeController {
    private final InventoryAppService inventoryAppService;
    private final FreezeMapper freezeMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody FreezePageQuery query) {
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
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        f -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", f.getId());
                                            m.put("freezeType", f.getFreezeType());
                                            m.put("warehouseId", f.getWarehouseId());
                                            m.put(
                                                    "stockId",
                                                    f.getStockId() != null ? f.getStockId() : 0);
                                            m.put("skuId", f.getSkuId() != null ? f.getSkuId() : 0);
                                            m.put("freezeQty", f.getFreezeQty());
                                            m.put(
                                                    "reason",
                                                    f.getReason() != null ? f.getReason() : "");
                                            m.put("status", f.getStatus());
                                            m.put(
                                                    "freezeAt",
                                                    f.getFreezeAt() != null
                                                            ? f.getFreezeAt().toString()
                                                            : "");
                                            m.put(
                                                    "releaseAt",
                                                    f.getReleaseAt() != null
                                                            ? f.getReleaseAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "库存管理", action = "冻结库存")
    public ApiResponse<FreezeDTO> create(@Valid @RequestBody FreezeCreateCmd cmd) {
        return ApiResponse.ok(inventoryAppService.createFreeze(cmd));
    }

    @PostMapping("/{id}/release")
    @OperationLog(module = "库存管理", action = "解冻库存")
    public ApiResponse<Void> release(@PathVariable("id") Long id) {
        inventoryAppService.releaseFreeze(id);
        return ApiResponse.ok();
    }
}
