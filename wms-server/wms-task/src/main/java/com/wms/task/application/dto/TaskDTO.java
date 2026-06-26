package com.wms.task.application.dto;

import com.wms.task.domain.entity.TaskHeader.Status;
import com.wms.task.domain.entity.TaskHeader.TaskType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String taskNo;
    private TaskType taskType;
    private Long warehouseId;
    private Status status;
    private Integer priority;
    private Long assignTo;
    private String taskSourceNo;
    private LocalDateTime createdAt;
}
