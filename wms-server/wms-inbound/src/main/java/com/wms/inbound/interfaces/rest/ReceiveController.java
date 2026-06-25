package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.application.service.ReceiveAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound/receives")
@RequiredArgsConstructor
public class ReceiveController {
    private final ReceiveAppService receiveAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<ReceiveDTO>> page(@Valid @RequestBody ReceivePageQuery query) {
        IPage<ReceiveDTO> result = receiveAppService.pageReceives(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReceiveDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(receiveAppService.getReceive(id));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "收货")
    public ApiResponse<ReceiveResultDTO> receive(@Valid @RequestBody ReceiveCreateCmd cmd) {
        return ApiResponse.ok(receiveAppService.receive(cmd));
    }
}
