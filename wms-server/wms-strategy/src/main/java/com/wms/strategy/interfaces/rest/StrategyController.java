package com.wms.strategy.interfaces.rest;

import com.wms.common.base.ApiResponse;
import com.wms.common.log.OperationLog;
import com.wms.strategy.application.dto.*;
import com.wms.strategy.application.service.StrategyAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/strategy")
@RequiredArgsConstructor
public class StrategyController {
    private final StrategyAppService strategyAppService;

    @GetMapping("/configs")
    public ApiResponse<List<StrategyConfigDTO>> listAll() {
        return ApiResponse.ok(strategyAppService.listAll());
    }

    @GetMapping("/configs/type/{type}")
    public ApiResponse<List<StrategyConfigDTO>> listByType(@PathVariable("type") String type) {
        return ApiResponse.ok(strategyAppService.listByType(type));
    }

    @GetMapping("/configs/{id}")
    public ApiResponse<StrategyConfigDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(strategyAppService.getById(id));
    }

    @PostMapping("/configs")
    @OperationLog(module = "策略管理", action = "创建策略")
    public ApiResponse<StrategyConfigDTO> createConfig(@Valid @RequestBody StrategyConfigCreateCmd cmd) {
        return ApiResponse.ok(strategyAppService.createConfig(cmd));
    }

    @DeleteMapping("/configs/{id}")
    @OperationLog(module = "策略管理", action = "删除策略")
    public ApiResponse<Void> deleteConfig(@PathVariable("id") Long id) {
        strategyAppService.deleteConfig(id);
        return ApiResponse.ok();
    }

    @PostMapping("/rules")
    @OperationLog(module = "策略管理", action = "添加规则")
    public ApiResponse<StrategyRuleDTO> addRule(@Valid @RequestBody StrategyRuleCreateCmd cmd) {
        return ApiResponse.ok(strategyAppService.addRule(cmd));
    }

    @PutMapping("/rules/{id}")
    @OperationLog(module = "策略管理", action = "更新规则")
    public ApiResponse<Void> updateRule(@PathVariable("id") Long id, @Valid @RequestBody StrategyRuleCreateCmd cmd) {
        strategyAppService.updateRule(id, cmd);
        return ApiResponse.ok();
    }

    @DeleteMapping("/rules/{id}")
    @OperationLog(module = "策略管理", action = "删除规则")
    public ApiResponse<Void> deleteRule(@PathVariable("id") Long id) {
        strategyAppService.deleteRule(id);
        return ApiResponse.ok();
    }

    @PostMapping("/match")
    public ApiResponse<StrategyMatchResultDTO> match(@Valid @RequestBody StrategyMatchRequest request) {
        return ApiResponse.ok(strategyAppService.match(request));
    }
}
