package com.wms.masterdata.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.LocationAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationAppService locationAppService;

    @GetMapping("/by-area/{areaId}")
    public ApiResponse<List<LocationDTO>> listByArea(@PathVariable("areaId") Long areaId) {
        return ApiResponse.ok(locationAppService.findByArea(areaId));
    }

    @PostMapping("/page")
    public ApiResponse<PageResponse<LocationDTO>> page(@Valid @RequestBody LocationPageQuery query) {
        IPage<LocationDTO> result = locationAppService.page(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<LocationDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(locationAppService.findById(id));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建库位")
    public ApiResponse<LocationDTO> create(@Valid @RequestBody LocationCreateCmd cmd) {
        return ApiResponse.ok(locationAppService.create(cmd));
    }

    @PostMapping("/batch")
    @OperationLog(module = "基础数据", action = "批量生成库位")
    public ApiResponse<List<LocationDTO>> batchCreate(@Valid @RequestBody LocationBatchCreateCmd cmd) {
        return ApiResponse.ok(locationAppService.batchCreate(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新库位")
    public ApiResponse<LocationDTO> update(@PathVariable("id") Long id, @Valid @RequestBody LocationUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(locationAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除库位")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        locationAppService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        locationAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        locationAppService.enable(id);
        return ApiResponse.ok();
    }
}
