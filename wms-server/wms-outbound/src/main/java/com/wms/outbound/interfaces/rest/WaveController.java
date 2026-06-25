package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.WaveAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound/waves")
@RequiredArgsConstructor
public class WaveController {
    private final WaveAppService waveAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<WaveDTO>> page(@Valid @RequestBody WavePageQuery query) {
        IPage<WaveDTO> result = waveAppService.pageWaves(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "出库管理", action = "生成波次")
    public ApiResponse<WaveResultDTO> create(@Valid @RequestBody WaveCreateCmd cmd) {
        return ApiResponse.ok(waveAppService.createWave(cmd));
    }

    @GetMapping("/{id}")
    public ApiResponse<WaveDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(waveAppService.getWave(id));
    }
}
