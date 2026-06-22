package com.wms.masterdata.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.SkuAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuAppService skuAppService;

    @GetMapping("/by-owner/{ownerId}")
    public ApiResponse<List<SkuDTO>> listByOwner(@PathVariable("ownerId") Long ownerId) {
        return ApiResponse.ok(skuAppService.findByOwner(ownerId));
    }

    @PostMapping("/page")
    public ApiResponse<PageResponse<SkuDTO>> page(@Valid @RequestBody SkuPageQuery query) {
        IPage<SkuDTO> result = skuAppService.page(query);
        return ApiResponse.ok(PageResponse.of(result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SkuDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(skuAppService.findById(id));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建SKU")
    public ApiResponse<SkuDTO> create(@Valid @RequestBody SkuCreateCmd cmd) {
        return ApiResponse.ok(skuAppService.create(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新SKU")
    public ApiResponse<SkuDTO> update(@PathVariable("id") Long id, @Valid @RequestBody SkuUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(skuAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除SKU")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        skuAppService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        skuAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        skuAppService.enable(id);
        return ApiResponse.ok();
    }

    // ───── 包装规格 ─────

    @GetMapping("/{skuId}/packages")
    public ApiResponse<List<SkuPackageDTO>> listPackages(@PathVariable("skuId") Long skuId) {
        return ApiResponse.ok(skuAppService.listPackages(skuId));
    }

    @PostMapping("/{skuId}/packages")
    @OperationLog(module = "基础数据", action = "添加SKU包装规格")
    public ApiResponse<SkuPackageDTO> createPackage(@PathVariable("skuId") Long skuId,
                                                     @Valid @RequestBody SkuPackageCreateCmd cmd) {
        cmd.setSkuId(skuId);
        return ApiResponse.ok(skuAppService.createPackage(cmd));
    }

    @DeleteMapping("/packages/{id}")
    @OperationLog(module = "基础数据", action = "删除SKU包装规格")
    public ApiResponse<Void> deletePackage(@PathVariable("id") Long id) {
        skuAppService.deletePackage(id);
        return ApiResponse.ok();
    }
}
