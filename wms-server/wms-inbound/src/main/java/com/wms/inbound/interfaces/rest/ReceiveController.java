package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.ReceiveCreateCmd;
import com.wms.inbound.application.dto.ReceivePageQuery;
import com.wms.inbound.application.service.InboundAppService;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;
import com.wms.inbound.infrastructure.mapper.AsnHeaderMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;
import com.wms.inbound.infrastructure.mapper.ReceiveLineMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/receives")
@RequiredArgsConstructor
public class ReceiveController {
    private final InboundAppService inboundAppService;
    private final ReceiveHeaderMapper receiveHeaderMapper;
    private final ReceiveLineMapper receiveLineMapper;
    private final AsnHeaderMapper asnHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody ReceivePageQuery query) {
        LambdaQueryWrapper<ReceiveHeader> wrapper =
                new LambdaQueryWrapper<ReceiveHeader>()
                        .eq(
                                query.getWarehouseId() != null,
                                ReceiveHeader::getWarehouseId,
                                query.getWarehouseId())
                        .eq(
                                query.getOwnerId() != null,
                                ReceiveHeader::getOwnerId,
                                query.getOwnerId())
                        .eq(query.getStatus() != null, ReceiveHeader::getStatus, query.getStatus())
                        .orderByDesc(ReceiveHeader::getCreatedAt);

        // asnNo filtering: resolve to asnHeaderIds first
        if (query.getAsnNo() != null && !query.getAsnNo().isBlank()) {
            List<Long> asnIds =
                    asnHeaderMapper
                            .selectList(
                                    new LambdaQueryWrapper<AsnHeader>()
                                            .like(AsnHeader::getAsnNo, query.getAsnNo())
                                            .select(AsnHeader::getId))
                            .stream()
                            .map(AsnHeader::getId)
                            .toList();
            if (asnIds.isEmpty()) {
                return ApiResponse.ok(
                        PageResponse.of(List.of(), 0L, query.getPageNum(), query.getPageSize()));
            }
            wrapper.in(ReceiveHeader::getAsnHeaderId, asnIds);
        }

        IPage<ReceiveHeader> result =
                receiveHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream().map(h -> buildReceiveMap(h)).toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable("id") Long id) {
        ReceiveHeader h = receiveHeaderMapper.selectById(id);
        if (h == null) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.ok(buildReceiveMap(h));
    }

    private Map<String, Object> buildReceiveMap(ReceiveHeader h) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", h.getId());
        m.put("receiveNo", h.getReceiveNo());
        m.put("warehouseId", h.getWarehouseId());
        m.put("ownerId", h.getOwnerId());
        m.put("asnHeaderId", h.getAsnHeaderId());
        m.put("receiveType", h.getReceiveType() != null ? h.getReceiveType() : "");
        m.put("status", h.getStatus());
        m.put("remark", h.getRemark());
        m.put("receivedBy", h.getReceivedBy());
        m.put("receivedAt", h.getReceivedAt() != null ? h.getReceivedAt().toString() : "");
        m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");

        // resolve asnNo
        if (h.getAsnHeaderId() != null) {
            AsnHeader asn = asnHeaderMapper.selectById(h.getAsnHeaderId());
            m.put("asnNo", asn != null ? asn.getAsnNo() : "");
        } else {
            m.put("asnNo", "");
        }

        // load lines
        List<ReceiveLine> lines =
                receiveLineMapper.selectList(
                        new LambdaQueryWrapper<ReceiveLine>()
                                .eq(ReceiveLine::getReceiveHeaderId, h.getId())
                                .orderByAsc(ReceiveLine::getLineNo));
        List<Map<String, Object>> lineList = new ArrayList<>();
        if (lines != null) {
            for (ReceiveLine l : lines) {
                Map<String, Object> lm = new HashMap<>();
                lm.put("id", l.getId());
                lm.put("lineNo", l.getLineNo());
                lm.put("skuId", l.getSkuId());
                lm.put("skuCode", l.getSkuCode());
                lm.put("skuName", l.getSkuName());
                lm.put("receiveQty", l.getReceiveQty());
                lm.put("receivePackage", l.getReceivePackage());
                lm.put("receiveLocationId", l.getReceiveLocationId());
                lm.put("batchNo", l.getBatchNo());
                lm.put("lotAttrs", l.getLotAttrs());
                lm.put(
                        "productionDate",
                        l.getProductionDate() != null ? l.getProductionDate().toString() : "");
                lm.put("expiryDate", l.getExpiryDate() != null ? l.getExpiryDate().toString() : "");
                lineList.add(lm);
            }
        }
        m.put("lines", lineList);
        return m;
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "收货")
    public ApiResponse<Object> receive(@Valid @RequestBody ReceiveCreateCmd cmd) {
        return ApiResponse.ok(inboundAppService.receive(cmd));
    }
}
