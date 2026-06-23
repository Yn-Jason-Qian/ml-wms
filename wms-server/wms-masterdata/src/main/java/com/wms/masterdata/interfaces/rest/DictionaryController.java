package com.wms.masterdata.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.common.log.OperationLog;
import com.wms.masterdata.application.dto.*;
import com.wms.masterdata.application.service.DictTypeAppService;
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
    private final DictTypeAppService dictTypeAppService;

    /** 获取所有字典类型（含中文名称） */
    @GetMapping("/types")
    public ApiResponse<List<DictTypeDTO>> listTypes() {
        return ApiResponse.ok(dictTypeAppService.listAll());
    }

    /** 新建字典类型 */
    @PostMapping("/types")
    @OperationLog(module = "基础数据", action = "创建字典类型")
    public ApiResponse<DictTypeDTO> createType(@Valid @RequestBody DictTypeCreateCmd cmd) {
        return ApiResponse.ok(dictTypeAppService.create(cmd));
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

    @PostMapping("/{id}/disable")
    @OperationLog(module = "基础数据", action = "禁用字典项")
    public ApiResponse<Void> disable(@PathVariable("id") Long id) {
        dictAppService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    @OperationLog(module = "基础数据", action = "启用字典项")
    public ApiResponse<Void> enable(@PathVariable("id") Long id) {
        dictAppService.enable(id);
        return ApiResponse.ok();
    }
}
