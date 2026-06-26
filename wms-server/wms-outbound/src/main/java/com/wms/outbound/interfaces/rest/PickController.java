package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.PickAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound/picks")
@RequiredArgsConstructor
public class PickController {
    private final PickAppService pickAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<PickDTO>> page(@Valid @RequestBody PickPageQuery query) {
        IPage<PickDTO> result = pickAppService.pagePicks(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/from-wave/{waveId}")
    @OperationLog(module = "出库管理", action = "生成拣货单")
    public ApiResponse<PickResultDTO> createPick(@PathVariable("waveId") Long waveId) {
        return ApiResponse.ok(pickAppService.createPickForWave(waveId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PickDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(pickAppService.getPickDetail(id));
    }

    @PostMapping("/submit")
    @OperationLog(module = "出库管理", action = "拣货确认")
    public ApiResponse<Void> submit(@Valid @RequestBody PickSubmitCmd cmd) {
        pickAppService.submitPick(cmd);
        return ApiResponse.ok();
    }
}
