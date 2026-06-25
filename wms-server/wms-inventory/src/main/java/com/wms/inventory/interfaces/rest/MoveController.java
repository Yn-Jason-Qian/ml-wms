package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.MoveCreateCmd;
import com.wms.inventory.application.dto.MoveDTO;
import com.wms.inventory.application.dto.MovePageQuery;
import com.wms.inventory.application.service.MoveAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/moves")
@RequiredArgsConstructor
public class MoveController {
    private final MoveAppService moveAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<MoveDTO>> page(@Valid @RequestBody MovePageQuery query) {
        IPage<MoveDTO> result = moveAppService.pageMove(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "库存管理", action = "创建移库单")
    public ApiResponse<MoveDTO> create(@Valid @RequestBody MoveCreateCmd cmd) {
        return ApiResponse.ok(moveAppService.createMove(cmd));
    }
}
