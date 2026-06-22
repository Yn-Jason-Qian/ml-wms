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
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.infrastructure.mapper.ReceiveHeaderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/receives")
@RequiredArgsConstructor
public class ReceiveController {
    private final InboundAppService inboundAppService;
    private final ReceiveHeaderMapper receiveHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(@Valid @RequestBody ReceivePageQuery query) {
        IPage<ReceiveHeader> result = receiveHeaderMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<ReceiveHeader>()
                        .eq(query.getWarehouseId() != null, ReceiveHeader::getWarehouseId, query.getWarehouseId())
                        .eq(query.getStatus() != null, ReceiveHeader::getStatus, query.getStatus())
                        .orderByDesc(ReceiveHeader::getCreatedAt));
        return ApiResponse.ok(PageResponse.of(result.getRecords().stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId()); m.put("receiveNo", h.getReceiveNo()); m.put("warehouseId", h.getWarehouseId());
            m.put("status", h.getStatus()); m.put("receiveType", h.getReceiveType() != null ? h.getReceiveType() : "");
            m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
            return m;
        }).toList(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "收货")
    public ApiResponse<Object> receive(@Valid @RequestBody ReceiveCreateCmd cmd) {
        return ApiResponse.ok(inboundAppService.receive(cmd));
    }
}
