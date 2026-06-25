package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.*;
import com.wms.inbound.application.service.QcAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound/qcs")
@RequiredArgsConstructor
public class QcController {
    private final QcAppService qcAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<QcDTO>> page(@Valid @RequestBody QcPageQuery query) {
        IPage<QcDTO> result = qcAppService.pageQcs(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<QcDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(qcAppService.getQc(id));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "创建质检单")
    public ApiResponse<QcResultDTO> create(@Valid @RequestBody QcCreateCmd cmd) {
        return ApiResponse.ok(qcAppService.createQc(cmd));
    }

    @PostMapping("/submit")
    @OperationLog(module = "入库管理", action = "提交质检结果")
    public ApiResponse<Void> submit(@Valid @RequestBody QcSubmitCmd cmd) {
        qcAppService.submitQcResult(cmd);
        return ApiResponse.ok();
    }
}
