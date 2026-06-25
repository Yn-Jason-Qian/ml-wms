package com.wms.outbound.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.outbound.application.dto.*;
import com.wms.outbound.application.service.OutboundAppService;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.infrastructure.mapper.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/outbound")
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundAppService outboundAppService;
    private final OrderHeaderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final WaveHeaderMapper waveMapper;
    private final WaveLineMapper waveLineMapper;
    private final PickHeaderMapper pickMapper;
    private final ShipHeaderMapper shipMapper;

    // ── Orders ──
    @PostMapping("/orders/page")
    public ApiResponse<PageResponse<Map<String, Object>>> pageOrders(
            @Valid @RequestBody OrderPageQuery query) {
        IPage<OrderHeader> result =
                orderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<OrderHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        OrderHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        OrderHeader::getStatus,
                                        query.getStatus())
                                .eq(
                                        query.getOrderType() != null,
                                        OrderHeader::getOrderType,
                                        query.getOrderType())
                                .orderByDesc(OrderHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("orderNo", h.getOrderNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("ownerId", h.getOwnerId());
                                            m.put("orderType", h.getOrderType());
                                            m.put(
                                                    "customerName",
                                                    h.getCustomerName() != null
                                                            ? h.getCustomerName()
                                                            : "");
                                            m.put("priority", h.getPriority());
                                            m.put("status", h.getStatus());
                                            m.put(
                                                    "createdAt",
                                                    h.getCreatedAt() != null
                                                            ? h.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/orders")
    @OperationLog(module = "出库管理", action = "创建订单")
    public ApiResponse<Map<String, Object>> createOrder(@Valid @RequestBody OrderCreateCmd cmd) {
        return ApiResponse.ok(outboundAppService.createOrder(cmd));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> getOrderById(@PathVariable("id") Long id) {
        OrderHeader h = orderMapper.selectById(id);
        if (h == null) {
            return ApiResponse.notFound("订单不存在");
        }
        List<OrderLine> lines =
                orderLineMapper.selectList(
                        new LambdaQueryWrapper<OrderLine>()
                                .eq(OrderLine::getOrderHeaderId, id)
                                .orderByAsc(OrderLine::getLineNo));
        Map<String, Object> result = new HashMap<>();
        result.put("id", h.getId());
        result.put("orderNo", h.getOrderNo());
        result.put("warehouseId", h.getWarehouseId());
        result.put("ownerId", h.getOwnerId());
        result.put("orderType", h.getOrderType());
        result.put("customerName", h.getCustomerName() != null ? h.getCustomerName() : "");
        result.put("customerAddress", h.getCustomerAddress() != null ? h.getCustomerAddress() : "");
        result.put("priority", h.getPriority());
        result.put("status", h.getStatus());
        result.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
        result.put(
                "lines",
                lines.stream()
                        .map(
                                l -> {
                                    Map<String, Object> lm = new HashMap<>();
                                    lm.put("id", l.getId());
                                    lm.put("lineNo", l.getLineNo());
                                    lm.put("skuId", l.getSkuId());
                                    lm.put("skuCode", l.getSkuCode());
                                    lm.put("skuName", l.getSkuName());
                                    lm.put("orderQty", l.getOrderQty());
                                    lm.put("allocatedQty", l.getAllocatedQty());
                                    lm.put("pickedQty", l.getPickedQty());
                                    lm.put("shippedQty", l.getShippedQty());
                                    lm.put("batchNo", l.getBatchNo() != null ? l.getBatchNo() : "");
                                    lm.put("status", l.getStatus());
                                    return lm;
                                })
                        .collect(Collectors.toList()));
        return ApiResponse.ok(result);
    }

    // ── Waves ──
    @PostMapping("/waves/page")
    public ApiResponse<PageResponse<Map<String, Object>>> pageWaves(
            @Valid @RequestBody WavePageQuery query) {
        IPage<WaveHeader> result =
                waveMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<WaveHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        WaveHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getWaveStatus() != null,
                                        WaveHeader::getWaveStatus,
                                        query.getWaveStatus())
                                .orderByDesc(WaveHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("waveNo", h.getWaveNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("waveType", h.getWaveType());
                                            m.put("waveStatus", h.getWaveStatus());
                                            m.put("orderCount", h.getOrderCount());
                                            m.put(
                                                    "createdAt",
                                                    h.getCreatedAt() != null
                                                            ? h.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/waves")
    @OperationLog(module = "出库管理", action = "生成波次")
    public ApiResponse<Map<String, Object>> createWave(@Valid @RequestBody WaveCreateCmd cmd) {
        return ApiResponse.ok(outboundAppService.createWave(cmd));
    }

    @GetMapping("/waves/{id}")
    public ApiResponse<Map<String, Object>> getWaveById(@PathVariable("id") Long id) {
        WaveHeader h = waveMapper.selectById(id);
        if (h == null) {
            return ApiResponse.notFound("波次不存在");
        }
        List<WaveLine> waveLines =
                waveLineMapper.selectList(
                        new LambdaQueryWrapper<WaveLine>()
                                .eq(WaveLine::getWaveHeaderId, id)
                                .orderByAsc(WaveLine::getSortOrder));
        List<Map<String, Object>> orders = new ArrayList<>();
        for (WaveLine wl : waveLines) {
            OrderHeader order = orderMapper.selectById(wl.getOrderHeaderId());
            if (order != null) {
                Map<String, Object> om = new HashMap<>();
                om.put("id", order.getId());
                om.put("orderNo", order.getOrderNo());
                om.put("orderType", order.getOrderType());
                om.put(
                        "customerName",
                        order.getCustomerName() != null ? order.getCustomerName() : "");
                om.put("priority", order.getPriority());
                om.put("status", order.getStatus());
                orders.add(om);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", h.getId());
        result.put("waveNo", h.getWaveNo());
        result.put("warehouseId", h.getWarehouseId());
        result.put("waveType", h.getWaveType());
        result.put("waveStatus", h.getWaveStatus());
        result.put("orderCount", h.getOrderCount());
        result.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
        result.put("orders", orders);
        return ApiResponse.ok(result);
    }

    // ── Picks ──
    @PostMapping("/picks/page")
    public ApiResponse<PageResponse<Map<String, Object>>> pagePicks(
            @Valid @RequestBody PickPageQuery query) {
        IPage<PickHeader> result =
                pickMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<PickHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        PickHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        PickHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(PickHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("pickNo", h.getPickNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("pickType", h.getPickType());
                                            m.put("status", h.getStatus());
                                            m.put(
                                                    "assignTo",
                                                    h.getAssignTo() != null ? h.getAssignTo() : 0);
                                            m.put(
                                                    "createdAt",
                                                    h.getCreatedAt() != null
                                                            ? h.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/picks/from-wave/{waveId}")
    @OperationLog(module = "出库管理", action = "生成拣货单")
    public ApiResponse<Map<String, Object>> createPick(@PathVariable("waveId") Long waveId) {
        return ApiResponse.ok(outboundAppService.createPickForWave(waveId));
    }

    @PostMapping("/picks/submit")
    @OperationLog(module = "出库管理", action = "拣货确认")
    public ApiResponse<Void> submitPick(@Valid @RequestBody PickSubmitCmd cmd) {
        outboundAppService.submitPick(cmd);
        return ApiResponse.ok();
    }

    // ── Ships ──
    @PostMapping("/ships/page")
    public ApiResponse<PageResponse<Map<String, Object>>> pageShips(
            @Valid @RequestBody ShipPageQuery query) {
        IPage<ShipHeader> result =
                shipMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<ShipHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        ShipHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        ShipHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(ShipHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("shipNo", h.getShipNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("deliveryMethod", h.getDeliveryMethod());
                                            m.put(
                                                    "carrierName",
                                                    h.getCarrierName() != null
                                                            ? h.getCarrierName()
                                                            : "");
                                            m.put(
                                                    "trackingNo",
                                                    h.getTrackingNo() != null
                                                            ? h.getTrackingNo()
                                                            : "");
                                            m.put("status", h.getStatus());
                                            m.put(
                                                    "createdAt",
                                                    h.getCreatedAt() != null
                                                            ? h.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/ships")
    @OperationLog(module = "出库管理", action = "发货确认")
    public ApiResponse<Map<String, Object>> createShip(@Valid @RequestBody ShipCreateCmd cmd) {
        return ApiResponse.ok(outboundAppService.createShip(cmd));
    }
}
