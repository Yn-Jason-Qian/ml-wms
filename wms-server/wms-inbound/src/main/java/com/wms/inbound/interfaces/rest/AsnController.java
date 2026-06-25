package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.AsnCreateCmd;
import com.wms.inbound.application.dto.AsnDTO;
import com.wms.inbound.application.dto.AsnPageQuery;
import com.wms.inbound.application.service.AsnAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound/asns")
@RequiredArgsConstructor
public class AsnController {
    private final AsnAppService asnAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<AsnDTO>> page(@Valid @RequestBody AsnPageQuery query) {
        IPage<AsnDTO> result = asnAppService.pageAsns(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "创建ASN")
    public ApiResponse<AsnDTO> create(@Valid @RequestBody AsnCreateCmd cmd) {
        return ApiResponse.ok(asnAppService.createAsn(cmd));
    }

    @GetMapping("/{id}")
    public ApiResponse<AsnDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(asnAppService.getAsn(id));
    }
}
