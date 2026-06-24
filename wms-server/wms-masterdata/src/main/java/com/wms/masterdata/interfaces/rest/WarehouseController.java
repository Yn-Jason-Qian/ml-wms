package com.wms.masterdata.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.WarehouseCreateCmd;
import com.wms.masterdata.application.dto.WarehouseDTO;
import com.wms.masterdata.application.dto.WarehousePageQuery;
import com.wms.masterdata.application.dto.WarehouseUpdateCmd;
import com.wms.masterdata.application.service.WarehouseAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseAppService warehouseAppService;

    @GetMapping
    public ApiResponse<List<WarehouseDTO>> list() {
        return ApiResponse.ok(warehouseAppService.findAll());
    }

    @PostMapping("/page")
    public ApiResponse<PageResponse<WarehouseDTO>> page(
            @Valid @RequestBody WarehousePageQuery query) {
        IPage<WarehouseDTO> result = warehouseAppService.page(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(warehouseAppService.findById(id));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建仓库", desc = "创建仓库")
    public ApiResponse<WarehouseDTO> create(@Valid @RequestBody WarehouseCreateCmd cmd) {
        return ApiResponse.ok(warehouseAppService.create(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新仓库", desc = "更新仓库")
    public ApiResponse<WarehouseDTO> update(
            @PathVariable("id") Long id, @Valid @RequestBody WarehouseUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(warehouseAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除仓库", desc = "删除仓库")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        warehouseAppService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    @OperationLog(module = "基础数据", action = "禁用仓库", desc = "禁用仓库")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        warehouseAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    @OperationLog(module = "基础数据", action = "启用仓库", desc = "启用仓库")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        warehouseAppService.enable(id);
        return ApiResponse.ok();
    }
}
