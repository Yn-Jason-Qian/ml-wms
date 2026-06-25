package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.OrderAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderAppService orderAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<OrderDTO>> page(@Valid @RequestBody OrderPageQuery query) {
        IPage<OrderDTO> result = orderAppService.pageOrders(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "出库管理", action = "创建订单")
    public ApiResponse<OrderResultDTO> create(@Valid @RequestBody OrderCreateCmd cmd) {
        return ApiResponse.ok(orderAppService.createOrder(cmd));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(orderAppService.getOrder(id));
    }
}
