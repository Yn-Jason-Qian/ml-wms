package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.inventory.application.dto.*;
import com.wms.inventory.application.service.StockAppService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class StockController {
    private final StockAppService stockAppService;

    @PostMapping("/stocks/page")
    public ApiResponse<PageResponse<StockDTO>> pageStocks(@Valid @RequestBody StockQuery query) {
        IPage<StockDTO> result = stockAppService.pageStock(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @GetMapping("/stocks/{id}")
    public ApiResponse<StockDTO> getStock(@PathVariable("id") Long id) {
        return ApiResponse.ok(stockAppService.findStockById(id));
    }

    @GetMapping("/stocks/{stockId}/transactions")
    public ApiResponse<List<StockTransactionDTO>> getTransactions(
            @PathVariable("stockId") Long stockId) {
        return ApiResponse.ok(stockAppService.findTransactionsByStock(stockId));
    }

    @PostMapping("/transactions/page")
    public ApiResponse<PageResponse<StockTransactionDTO>> pageTransactions(
            @Valid @RequestBody TransactionQuery query) {
        IPage<StockTransactionDTO> result = stockAppService.pageTransactions(query);
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }
}
