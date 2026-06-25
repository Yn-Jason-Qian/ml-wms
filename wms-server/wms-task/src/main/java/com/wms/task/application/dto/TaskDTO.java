package com.wms.task.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String taskNo;
    private String taskType;
    private Long warehouseId;
    private String status;
    private Integer priority;
    private Long assignTo;
    private String taskSourceNo;
    private LocalDateTime createdAt;
}
