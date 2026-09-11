package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.CheckAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound/checks")
@RequiredArgsConstructor
public class CheckController {
    private final CheckAppService checkAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<CheckDTO>> page(@Valid @RequestBody CheckPageQuery query) {
        IPage<CheckDTO> result = checkAppService.pageChecks(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/from-wave/{waveId}")
    @OperationLog(module = "出库管理", action = "生成复核单")
    public ApiResponse<CheckResultDTO> createFromWave(@PathVariable("waveId") Long waveId) {
        return ApiResponse.ok(checkAppService.createCheckForWave(waveId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CheckDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(checkAppService.getCheckDetail(id));
    }

    @PostMapping("/submit")
    @OperationLog(module = "出库管理", action = "复核确认")
    public ApiResponse<Void> submit(@Valid @RequestBody CheckSubmitCmd cmd) {
        checkAppService.submitCheck(cmd);
        return ApiResponse.ok();
    }
}
