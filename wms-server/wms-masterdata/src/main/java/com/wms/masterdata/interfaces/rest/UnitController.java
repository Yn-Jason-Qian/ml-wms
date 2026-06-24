package com.wms.masterdata.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.masterdata.application.dto.UnitDTO;
import com.wms.masterdata.application.service.UnitAppService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitAppService unitAppService;

    @GetMapping
    public ApiResponse<List<UnitDTO>> list() {
        return ApiResponse.ok(unitAppService.findAll());
    }
}
