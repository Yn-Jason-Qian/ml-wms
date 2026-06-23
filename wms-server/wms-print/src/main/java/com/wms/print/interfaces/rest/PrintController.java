package com.wms.print.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.log.OperationLog;
import com.wms.print.application.dto.PrintCmd;
import com.wms.print.application.dto.PrintTemplateCreateCmd;
import com.wms.print.application.dto.PrintTemplateDTO;
import com.wms.print.application.service.PrintAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/prints")
@RequiredArgsConstructor
public class PrintController {

    private final PrintAppService printAppService;

    // ── 模板管理 ──

    @PostMapping("/templates")
    @OperationLog(module = "打印管理", action = "创建打印模板")
    public ApiResponse<PrintTemplateDTO> createTemplate(@Valid @RequestBody PrintTemplateCreateCmd cmd) {
        return ApiResponse.ok(printAppService.createTemplate(cmd));
    }

    @PostMapping("/templates/page")
    public ApiResponse<PageResponse<PrintTemplateDTO>> pageTemplates(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(printAppService.pageTemplates(pageNum, pageSize));
    }

    // ── 标签生成 ──

    @PostMapping("/zpl/generate")
    @OperationLog(module = "打印管理", action = "生成ZPL标签")
    public ApiResponse<Map<String, Object>> generateZpl(@RequestBody Map<String, Object> body) {
        String templateType = (String) body.getOrDefault("templateType", "RECEIVE_LABEL");
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) body.getOrDefault("data", Map.of());
        String zpl = printAppService.generateZpl(templateType, data);
        return ApiResponse.ok(Map.of("zpl", zpl));
    }

    // ── 执行打印 ──

    @PostMapping("/print")
    @OperationLog(module = "打印管理", action = "执行打印")
    public ApiResponse<Map<String, Object>> print(@Valid @RequestBody PrintCmd cmd) {
        return ApiResponse.ok(printAppService.executePrint(cmd));
    }
}
