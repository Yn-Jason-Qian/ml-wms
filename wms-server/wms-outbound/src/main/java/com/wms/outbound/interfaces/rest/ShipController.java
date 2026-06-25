package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.ShipAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound/ships")
@RequiredArgsConstructor
public class ShipController {
    private final ShipAppService shipAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<ShipDTO>> page(@Valid @RequestBody ShipPageQuery query) {
        IPage<ShipDTO> result = shipAppService.pageShips(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "出库管理", action = "发货确认")
    public ApiResponse<ShipResultDTO> create(@Valid @RequestBody ShipCreateCmd cmd) {
        return ApiResponse.ok(shipAppService.createShip(cmd));
    }
}
