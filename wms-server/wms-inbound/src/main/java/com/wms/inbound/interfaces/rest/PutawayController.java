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
import com.wms.inbound.infrastructure.mapper.PutawayHeaderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inbound/putaways")
@RequiredArgsConstructor
public class PutawayController {
    private final InboundAppService inboundAppService;
    private final PutawayHeaderMapper putawayHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(@Valid @RequestBody PutawayPageQuery query) {
        IPage<PutawayHeader> result = putawayHeaderMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<PutawayHeader>()
                        .eq(query.getWarehouseId() != null, PutawayHeader::getWarehouseId, query.getWarehouseId())
                        .eq(query.getStatus() != null, PutawayHeader::getStatus, query.getStatus())
                        .orderByDesc(PutawayHeader::getCreatedAt));
        return ApiResponse.ok(PageResponse.of(result.getRecords().stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId()); m.put("putawayNo", h.getPutawayNo()); m.put("warehouseId", h.getWarehouseId());
            m.put("status", h.getStatus()); m.put("receiveHeaderId", h.getReceiveHeaderId() != null ? h.getReceiveHeaderId() : 0);
            m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
            return m;
        }).toList(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize()));
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
