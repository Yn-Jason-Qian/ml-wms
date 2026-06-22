package com.wms.masterdata.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.OwnerAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerAppService ownerAppService;

    @GetMapping
    public ApiResponse<List<OwnerDTO>> list() {
        return ApiResponse.ok(ownerAppService.findAll());
    }

    @PostMapping("/page")
    public ApiResponse<PageResponse<OwnerDTO>> page(@Valid @RequestBody OwnerPageQuery query) {
        IPage<OwnerDTO> result = ownerAppService.page(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OwnerDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(ownerAppService.findById(id));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建货主")
    public ApiResponse<OwnerDTO> create(@Valid @RequestBody OwnerCreateCmd cmd) {
        return ApiResponse.ok(ownerAppService.create(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新货主")
    public ApiResponse<OwnerDTO> update(@PathVariable("id") Long id, @Valid @RequestBody OwnerUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(ownerAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除货主")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        ownerAppService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        ownerAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        ownerAppService.enable(id);
        return ApiResponse.ok();
    }
}
