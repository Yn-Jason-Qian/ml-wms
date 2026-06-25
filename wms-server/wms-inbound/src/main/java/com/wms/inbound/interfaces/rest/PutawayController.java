package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.PutawayCreateCmd;
import com.wms.inbound.application.dto.PutawayPageQuery;
import com.wms.inbound.application.dto.PutawaySubmitCmd;
import com.wms.inbound.application.service.InboundAppService;
import com.wms.inbound.domain.entity.PutawayHeader;
import com.wms.inbound.domain.entity.PutawayLine;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.infrastructure.mapper.PutawayHeaderMapper;
import com.wms.inbound.infrastructure.mapper.PutawayLineMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/putaways")
@RequiredArgsConstructor
public class PutawayController {
    private final InboundAppService inboundAppService;
    private final PutawayHeaderMapper putawayHeaderMapper;
    private final PutawayLineMapper putawayLineMapper;
    private final ReceiveHeaderMapper receiveHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody PutawayPageQuery query) {
        IPage<PutawayHeader> result =
                putawayHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<PutawayHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        PutawayHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        PutawayHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(PutawayHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream().map(h -> buildPutawayMap(h)).toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable("id") Long id) {
        PutawayHeader h = putawayHeaderMapper.selectById(id);
        if (h == null) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.ok(buildPutawayMap(h));
    }

    private Map<String, Object> buildPutawayMap(PutawayHeader h) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", h.getId());
        m.put("putawayNo", h.getPutawayNo());
        m.put("warehouseId", h.getWarehouseId());
        m.put("receiveHeaderId", h.getReceiveHeaderId());
        m.put("status", h.getStatus());
        m.put("strategyId", h.getStrategyId());
        m.put("remark", h.getRemark());
        m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");

        // resolve receive header info
        if (h.getReceiveHeaderId() != null) {
            ReceiveHeader rcv = receiveHeaderMapper.selectById(h.getReceiveHeaderId());
            if (rcv != null) {
                m.put("receiveNo", rcv.getReceiveNo());
                m.put("ownerId", rcv.getOwnerId());
            } else {
                m.put("receiveNo", "");
                m.put("ownerId", null);
            }
        } else {
            m.put("receiveNo", "");
            m.put("ownerId", null);
        }

        // load lines
        List<PutawayLine> lines =
                putawayLineMapper.selectList(
                        new LambdaQueryWrapper<PutawayLine>()
                                .eq(PutawayLine::getPutawayHeaderId, h.getId())
                                .orderByAsc(PutawayLine::getLineNo));
        List<Map<String, Object>> lineList = new ArrayList<>();
        if (lines != null) {
            for (PutawayLine l : lines) {
                Map<String, Object> lm = new HashMap<>();
                lm.put("id", l.getId());
                lm.put("lineNo", l.getLineNo());
                lm.put("skuId", l.getSkuId());
                lm.put("skuCode", l.getSkuCode());
                lm.put("skuName", l.getSkuName());
                lm.put("putawayQty", l.getPutawayQty());
                lm.put("doneQty", l.getDoneQty());
                lm.put("fromLocationId", l.getFromLocationId());
                lm.put("toLocationId", l.getToLocationId());
                lm.put("batchNo", l.getBatchNo());
                lm.put("lotAttrs", l.getLotAttrs());
                lm.put("status", l.getStatus());
                lm.put("putawayAt", l.getPutawayAt() != null ? l.getPutawayAt().toString() : "");
                lineList.add(lm);
            }
        }
        m.put("lines", lineList);
        return m;
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "生成上架单")
    public ApiResponse<Object> create(@Valid @RequestBody PutawayCreateCmd cmd) {
        return ApiResponse.ok(inboundAppService.createPutaway(cmd));
    }

    @PostMapping("/submit")
    @OperationLog(module = "入库管理", action = "上架确认")
    public ApiResponse<Void> submit(@Valid @RequestBody PutawaySubmitCmd cmd) {
        inboundAppService.submitPutaway(cmd);
        return ApiResponse.ok();
    }
}
