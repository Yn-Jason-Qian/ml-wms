package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.QcCreateCmd;
import com.wms.inbound.application.dto.QcPageQuery;
import com.wms.inbound.application.dto.QcSubmitCmd;
import com.wms.inbound.application.service.InboundAppService;
import com.wms.inbound.domain.entity.QcHeader;
import com.wms.inbound.domain.entity.QcLine;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.infrastructure.mapper.QcHeaderMapper;
import com.wms.inbound.infrastructure.mapper.QcLineMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/qcs")
@RequiredArgsConstructor
public class QcController {
    private final InboundAppService inboundAppService;
    private final QcHeaderMapper qcHeaderMapper;
    private final QcLineMapper qcLineMapper;
    private final ReceiveHeaderMapper receiveHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody QcPageQuery query) {
        IPage<QcHeader> result =
                qcHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<QcHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        QcHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        QcHeader::getStatus,
                                        query.getStatus())
                                .orderByDesc(QcHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream().map(h -> buildQcMap(h)).toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable("id") Long id) {
        QcHeader h = qcHeaderMapper.selectById(id);
        if (h == null) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.ok(buildQcMap(h));
    }

    private Map<String, Object> buildQcMap(QcHeader h) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", h.getId());
        m.put("qcNo", h.getQcNo());
        m.put("warehouseId", h.getWarehouseId());
        m.put("receiveHeaderId", h.getReceiveHeaderId());
        m.put("qcType", h.getQcType() != null ? h.getQcType() : "");
        m.put("sampleRatio", h.getSampleRatio());
        m.put("status", h.getStatus());
        m.put("remark", h.getRemark());
        m.put("qcBy", h.getQcBy());
        m.put("qcAt", h.getQcAt() != null ? h.getQcAt().toString() : "");
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
        List<QcLine> lines =
                qcLineMapper.selectList(
                        new LambdaQueryWrapper<QcLine>()
                                .eq(QcLine::getQcHeaderId, h.getId())
                                .orderByAsc(QcLine::getLineNo));
        List<Map<String, Object>> lineList = new ArrayList<>();
        if (lines != null) {
            for (QcLine l : lines) {
                Map<String, Object> lm = new HashMap<>();
                lm.put("id", l.getId());
                lm.put("lineNo", l.getLineNo());
                lm.put("skuId", l.getSkuId());
                lm.put("skuCode", l.getSkuCode());
                lm.put("skuName", l.getSkuName());
                lm.put("inspectQty", l.getInspectQty());
                lm.put("passQty", l.getPassQty());
                lm.put("rejectQty", l.getRejectQty());
                lm.put("rejectReason", l.getRejectReason());
                lm.put("batchNo", l.getBatchNo());
                lm.put("lotAttrs", l.getLotAttrs());
                lineList.add(lm);
            }
        }
        m.put("lines", lineList);
        return m;
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "创建质检单")
    public ApiResponse<Object> create(@Valid @RequestBody QcCreateCmd cmd) {
        return ApiResponse.ok(inboundAppService.createQc(cmd));
    }

    @PostMapping("/submit")
    @OperationLog(module = "入库管理", action = "提交质检结果")
    public ApiResponse<Void> submit(@Valid @RequestBody QcSubmitCmd cmd) {
        inboundAppService.submitQcResult(cmd);
        return ApiResponse.ok();
    }
}
