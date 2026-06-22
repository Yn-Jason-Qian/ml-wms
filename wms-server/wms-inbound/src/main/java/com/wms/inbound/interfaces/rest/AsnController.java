package com.wms.inbound.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inbound.application.dto.AsnCreateCmd;
import com.wms.inbound.application.dto.AsnDTO;
import com.wms.inbound.application.dto.AsnPageQuery;
import com.wms.inbound.application.service.InboundAppService;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.infrastructure.mapper.AsnHeaderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound/asns")
@RequiredArgsConstructor
public class AsnController {
    private final InboundAppService inboundAppService;
    private final AsnHeaderMapper asnHeaderMapper;

    @PostMapping("/page")
    public ApiResponse<PageResponse<AsnDTO>> page(@Valid @RequestBody AsnPageQuery query) {
        IPage<AsnHeader> result = asnHeaderMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<AsnHeader>()
                        .eq(query.getWarehouseId() != null, AsnHeader::getWarehouseId, query.getWarehouseId())
                        .eq(query.getStatus() != null, AsnHeader::getStatus, query.getStatus())
                        .like(query.getAsnNo() != null, AsnHeader::getAsnNo, query.getAsnNo())
                        .orderByDesc(AsnHeader::getCreatedAt));
        return ApiResponse.ok(PageResponse.of(result.getRecords().stream().map(h -> {
            AsnDTO d = new AsnDTO(); d.setId(h.getId()); d.setAsnNo(h.getAsnNo());
            d.setWarehouseId(h.getWarehouseId()); d.setOwnerId(h.getOwnerId());
            d.setAsnType(h.getAsnType()); d.setStatus(h.getStatus());
            d.setCreatedAt(h.getCreatedAt()); return d;
        }).toList(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "入库管理", action = "创建ASN")
    public ApiResponse<AsnDTO> create(@Valid @RequestBody AsnCreateCmd cmd) {
        return ApiResponse.ok(inboundAppService.createAsn(cmd));
    }

    @GetMapping("/{id}")
    public ApiResponse<AsnDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(inboundAppService.getAsn(id));
    }
}
