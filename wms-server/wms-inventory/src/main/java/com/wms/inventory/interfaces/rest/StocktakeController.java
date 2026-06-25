package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.inventory.application.dto.StocktakeCreateCmd;
import com.wms.inventory.application.dto.StocktakeDTO;
import com.wms.inventory.application.dto.StocktakePageQuery;
import com.wms.inventory.application.service.StocktakeAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/stocktakes")
@RequiredArgsConstructor
public class StocktakeController {
    private final StocktakeAppService stocktakeAppService;

    @PostMapping("/page")
    public ApiResponse<PageResponse<StocktakeDTO>> page(
            @Valid @RequestBody StocktakePageQuery query) {
        IPage<StocktakeDTO> result = stocktakeAppService.pageStocktake(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping
    @OperationLog(module = "库存管理", action = "创建盘点单")
    public ApiResponse<StocktakeDTO> create(@Valid @RequestBody StocktakeCreateCmd cmd) {
        return ApiResponse.ok(stocktakeAppService.createStocktake(cmd));
    }
}
