package com.wms.masterdata.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.DictionaryAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata/dict")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryAppService dictAppService;

    @GetMapping("/types")
    public ApiResponse<List<String>> listTypes() {
        return ApiResponse.ok(dictAppService.listTypes());
    }

    @GetMapping("/items/{dictType}")
    public ApiResponse<List<DictionaryDTO>> listByType(@PathVariable("dictType") String dictType) {
        return ApiResponse.ok(dictAppService.listByType(dictType));
    }

    @PostMapping
    @OperationLog(module = "基础数据", action = "创建字典项")
    public ApiResponse<DictionaryDTO> create(@Valid @RequestBody DictionaryCreateCmd cmd) {
        return ApiResponse.ok(dictAppService.create(cmd));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "基础数据", action = "更新字典项")
    public ApiResponse<DictionaryDTO> update(@PathVariable("id") Long id,
                                              @Valid @RequestBody DictionaryUpdateCmd cmd) {
        cmd.setId(id);
        return ApiResponse.ok(dictAppService.update(cmd));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "基础数据", action = "删除字典项")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        dictAppService.delete(id);
        return ApiResponse.ok();
    }
}
