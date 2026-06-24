package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.MoveCreateCmd;
import com.wms.inventory.application.dto.MoveDTO;
import com.wms.inventory.application.dto.MovePageQuery;
import com.wms.inventory.application.service.InventoryAppService;
import com.wms.inventory.domain.entity.MoveHeader;
import com.wms.inventory.infrastructure.mapper.MoveHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory/moves")
@RequiredArgsConstructor
public class MoveController {
    private final InventoryAppService inventoryAppService;
    private final MoveHeaderMapper moveHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody MovePageQuery query) {
        IPage<MoveHeader> result =
                moveHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<MoveHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        MoveHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        MoveHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(MoveHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("moveNo", h.getMoveNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("moveType", h.getMoveType());
                                            m.put("status", h.getStatus());
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
    @OperationLog(module = "库存管理", action = "创建移库单")
    public ApiResponse<MoveDTO> create(@Valid @RequestBody MoveCreateCmd cmd) {
        return ApiResponse.ok(inventoryAppService.createMove(cmd));
    }
}
