package com.wms.masterdata.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.AreaAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaAppService areaAppService;

    @GetMapping("/by-warehouse/{warehouseId}")
    public ApiResponse<List<AreaDTO>> listByWarehouse(@PathVariable("warehouseId") Long warehouseId) {
        return ApiResponse.ok(areaAppService.findByWarehouse(warehouseId));
    }

    @PostMapping("/page")
    public ApiResponse<PageResponse<AreaDTO>> page(@Valid @RequestBody AreaPageQuery query) {
        IPage<AreaDTO> result = areaAppService.page(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<AreaDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(areaAppService.findById(id));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建库区")
    public ApiResponse<AreaDTO> create(@Valid @RequestBody AreaCreateCmd cmd) {
        return ApiResponse.ok(areaAppService.create(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新库区")
    public ApiResponse<AreaDTO> update(@PathVariable("id") Long id, @Valid @RequestBody AreaUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(areaAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除库区")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        areaAppService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        areaAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        areaAppService.enable(id);
        return ApiResponse.ok();
    }
}
