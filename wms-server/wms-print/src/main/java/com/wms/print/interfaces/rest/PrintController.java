package com.wms.print.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.common.log.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prints")
@RequiredArgsConstructor
public class PrintController {

    @PostMapping("/templates")
    @OperationLog(module = "打印管理", action = "创建打印模板")
    public ApiResponse<Map<String, Object>> createTemplate(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(Map.of("templateId", 1));
    }

    @PostMapping("/print")
    @OperationLog(module = "打印管理", action = "执行打印")
    public ApiResponse<Void> print(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok();
    }
}
