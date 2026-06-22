package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import com.wms.common.util.EasyExcelUtil;
import com.wms.masterdata.domain.entity.*;
import com.wms.masterdata.infrastructure.mapper.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImportController {
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final OwnerMapper ownerMapper;
    private final SkuMapper skuMapper;

    @PostMapping("/import/warehouses") public ApiResponse<Integer> importWarehouses(@RequestParam("file") MultipartFile f) throws IOException {
        var list = new ArrayList<Warehouse>(); EasyExcelUtil.read(f.getInputStream(), Warehouse.class, batch -> list.addAll(batch));
        list.forEach(w -> { w.setStatus(1); warehouseMapper.insert(w); }); return ApiResponse.ok(list.size());
    }
    @PostMapping("/import/locations") public ApiResponse<Integer> importLocations(@RequestParam("file") MultipartFile f) throws IOException {
        var list = new ArrayList<Location>(); EasyExcelUtil.read(f.getInputStream(), Location.class, batch -> list.addAll(batch));
        list.forEach(l -> { l.setStatus(1); locationMapper.insert(l); }); return ApiResponse.ok(list.size());
    }
    @PostMapping("/import/owners") public ApiResponse<Integer> importOwners(@RequestParam("file") MultipartFile f) throws IOException {
        var list = new ArrayList<Owner>(); EasyExcelUtil.read(f.getInputStream(), Owner.class, batch -> list.addAll(batch));
        list.forEach(o -> { o.setStatus(1); ownerMapper.insert(o); }); return ApiResponse.ok(list.size());
    }
    @PostMapping("/import/skus") public ApiResponse<Integer> importSkus(@RequestParam("file") MultipartFile f) throws IOException {
        var list = new ArrayList<Sku>(); EasyExcelUtil.read(f.getInputStream(), Sku.class, batch -> list.addAll(batch));
        list.forEach(s -> { s.setStatus(1); skuMapper.insert(s); }); return ApiResponse.ok(list.size());
    }

    @GetMapping("/template/warehouses") public void tplWarehouses(HttpServletResponse r) throws IOException { EasyExcelUtil.export(r,"warehouse_template","仓库",Warehouse.class,Collections.singletonList(new Warehouse())); }
    @GetMapping("/template/locations") public void tplLocations(HttpServletResponse r) throws IOException { EasyExcelUtil.export(r,"location_template","库位",Location.class,Collections.singletonList(new Location())); }
    @GetMapping("/template/owners") public void tplOwners(HttpServletResponse r) throws IOException { EasyExcelUtil.export(r,"owner_template","货主",Owner.class,Collections.singletonList(new Owner())); }
    @GetMapping("/template/skus") public void tplSkus(HttpServletResponse r) throws IOException { EasyExcelUtil.export(r,"sku_template","SKU",Sku.class,Collections.singletonList(new Sku())); }
}
