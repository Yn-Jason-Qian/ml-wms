package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.application.service.PutawayAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound/putaways")
@RequiredArgsConstructor
public class PutawayController {
    private final PutawayAppService putawayAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<PutawayDTO>> page(@Valid @RequestBody PutawayPageQuery query) {
        IPage<PutawayDTO> result = putawayAppService.pagePutaways(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PutawayDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(putawayAppService.getPutaway(id));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "生成上架单")
    public ApiResponse<PutawayResultDTO> create(@Valid @RequestBody PutawayCreateCmd cmd) {
        return ApiResponse.ok(putawayAppService.createPutaway(cmd));
    }

    @PostMapping("/submit")
    @OperationLog(module = "入库管理", action = "上架确认")
    public ApiResponse<Void> submit(@Valid @RequestBody PutawaySubmitCmd cmd) {
        putawayAppService.submitPutaway(cmd);
        return ApiResponse.ok();
    }
}
