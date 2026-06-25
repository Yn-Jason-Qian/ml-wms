package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.FreezeCreateCmd;
import com.wms.inventory.application.dto.FreezeDTO;
import com.wms.inventory.application.dto.FreezePageQuery;
import com.wms.inventory.application.service.FreezeAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/freezes")
@RequiredArgsConstructor
public class FreezeController {
    private final FreezeAppService freezeAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<FreezeDTO>> page(@Valid @RequestBody FreezePageQuery query) {
        IPage<FreezeDTO> result = freezeAppService.pageFreeze(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "库存管理", action = "冻结库存")
    public ApiResponse<FreezeDTO> create(@Valid @RequestBody FreezeCreateCmd cmd) {
        return ApiResponse.ok(freezeAppService.createFreeze(cmd));
    }

    @PostMapping("/{id}/release")
    @OperationLog(module = "库存管理", action = "解冻库存")
    public ApiResponse<Void> release(@PathVariable("id") Long id) {
        freezeAppService.releaseFreeze(id);
        return ApiResponse.ok();
    }
}
