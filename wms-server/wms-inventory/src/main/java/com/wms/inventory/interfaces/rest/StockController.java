package com.wms.inventory.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.inventory.application.dto.*;
import com.wms.inventory.application.service.InventoryAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class StockController {
    private final InventoryAppService inventoryAppService;

    @PostMapping("/stocks/page")
    public ApiResponse<PageResponse<StockDTO>> pageStocks(@Valid @RequestBody StockQuery query) {
        IPage<StockDTO> result = inventoryAppService.pageStock(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }

    @GetMapping("/stocks/{id}")
    public ApiResponse<StockDTO> getStock(@PathVariable("id") Long id) {
        return ApiResponse.ok(inventoryAppService.findStockById(id));
    }

    @GetMapping("/stocks/{stockId}/transactions")
    public ApiResponse<List<StockTransactionDTO>> getTransactions(@PathVariable("stockId") Long stockId) {
        return ApiResponse.ok(inventoryAppService.findTransactionsByStock(stockId));
    }

    @PostMapping("/transactions/page")
    public ApiResponse<PageResponse<StockTransactionDTO>> pageTransactions(
            @Valid @RequestBody TransactionQuery query) {
        IPage<StockTransactionDTO> result = inventoryAppService.pageTransactions(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }
}
