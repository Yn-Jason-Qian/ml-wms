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
import com.wms.inbound.infrastructure.mapper.QcHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/qcs")
@RequiredArgsConstructor
public class QcController {
    private final InboundAppService inboundAppService;
    private final QcHeaderMapper qcHeaderMapper;

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
                        result.getRecords().stream()
                                .map(
                                        h -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", h.getId());
                                            m.put("qcNo", h.getQcNo());
                                            m.put("warehouseId", h.getWarehouseId());
                                            m.put("status", h.getStatus());
                                            m.put(
                                                    "qcType",
                                                    h.getQcType() != null ? h.getQcType() : "");
                                            m.put(
                                                    "receiveHeaderId",
                                                    h.getReceiveHeaderId() != null
                                                            ? h.getReceiveHeaderId()
                                                            : 0);
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
