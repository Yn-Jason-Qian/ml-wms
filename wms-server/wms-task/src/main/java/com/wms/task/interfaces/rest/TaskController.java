package com.wms.task.interfaces.rest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.ApiResponse;
import com.wms.common.base.PageResponse;
import com.wms.common.event.TaskEvent;
import com.wms.common.log.OperationLog;
import com.wms.task.application.dto.TaskPageQuery;
import com.wms.task.domain.entity.TaskHeader;
import com.wms.task.infrastructure.mapper.TaskHeaderMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskHeaderMapper taskHeaderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @Valid @RequestBody TaskPageQuery query) {
        IPage<TaskHeader> result =
                taskHeaderMapper.selectPage(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        new LambdaQueryWrapper<TaskHeader>()
                                .eq(
                                        query.getWarehouseId() != null,
                                        TaskHeader::getWarehouseId,
                                        query.getWarehouseId())
                                .eq(
                                        query.getStatus() != null,
                                        TaskHeader::getStatus,
                                        query.getStatus())
                                .eq(
                                        query.getTaskType() != null,
                                        TaskHeader::getTaskType,
                                        query.getTaskType())
                                .orderByDesc(TaskHeader::getCreatedAt));
        return ApiResponse.ok(
                PageResponse.of(
                        result.getRecords().stream()
                                .map(
                                        t -> {
                                            java.util.Map<String, Object> m =
                                                    new java.util.HashMap<>();
                                            m.put("id", t.getId());
                                            m.put("taskNo", t.getTaskNo());
                                            m.put("taskType", t.getTaskType());
                                            m.put("warehouseId", t.getWarehouseId());
                                            m.put("status", t.getStatus());
                                            m.put("priority", t.getPriority());
                                            m.put(
                                                    "assignTo",
                                                    t.getAssignTo() != null ? t.getAssignTo() : 0);
                                            m.put(
                                                    "taskSourceNo",
                                                    t.getTaskSourceNo() != null
                                                            ? t.getTaskSourceNo()
                                                            : "");
                                            m.put(
                                                    "createdAt",
                                                    t.getCreatedAt() != null
                                                            ? t.getCreatedAt().toString()
                                                            : "");
                                            return m;
                                        })
                                .toList(),
                        result.getTotal(),
                        (int) result.getCurrent(),
                        (int) result.getSize()));
    }

    @PostMapping("/{id}/claim")
    @OperationLog(module = "任务管理", action = "领取任务")
    public ApiResponse<Map<String, Object>> claim(@PathVariable("id") Long id) {
        TaskHeader task = taskHeaderMapper.selectById(id);
        if (task != null) {
            task.assign(task.getAssignTo()); // mark state transition
            taskHeaderMapper.updateById(task);
            eventPublisher.publishEvent(
                    new TaskEvent(
                            this,
                            "CLAIMED",
                            task.getId(),
                            task.getTaskNo(),
                            task.getTaskType(),
                            task.getWarehouseId(),
                            task.getAssignTo()));
        }
        return ApiResponse.ok(Map.of("taskId", id, "status", "ASSIGNED"));
    }

    @PostMapping("/{id}/start")
    @OperationLog(module = "任务管理", action = "开始执行")
    public ApiResponse<Void> start(@PathVariable("id") Long id) {
        TaskHeader task = taskHeaderMapper.selectById(id);
        if (task != null) {
            task.start();
            taskHeaderMapper.updateById(task);
            eventPublisher.publishEvent(
                    new TaskEvent(
                            this,
                            "STARTED",
                            task.getId(),
                            task.getTaskNo(),
                            task.getTaskType(),
                            task.getWarehouseId(),
                            task.getAssignTo()));
        }
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/complete")
    @OperationLog(module = "任务管理", action = "完成任务")
    public ApiResponse<Void> complete(@PathVariable("id") Long id) {
        TaskHeader task = taskHeaderMapper.selectById(id);
        if (task != null) {
            task.complete();
            taskHeaderMapper.updateById(task);
            eventPublisher.publishEvent(
                    new TaskEvent(
                            this,
                            "COMPLETED",
                            task.getId(),
                            task.getTaskNo(),
                            task.getTaskType(),
                            task.getWarehouseId(),
                            task.getAssignTo()));
        }
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/cancel")
    @OperationLog(module = "任务管理", action = "取消任务")
    public ApiResponse<Void> cancel(@PathVariable("id") Long id) {
        TaskHeader task = taskHeaderMapper.selectById(id);
        if (task != null) {
            task.cancel();
            taskHeaderMapper.updateById(task);
            eventPublisher.publishEvent(
                    new TaskEvent(
                            this,
                            "CANCELLED",
                            task.getId(),
                            task.getTaskNo(),
                            task.getTaskType(),
                            task.getWarehouseId(),
                            task.getAssignTo()));
        }
        return ApiResponse.ok();
    }
}
