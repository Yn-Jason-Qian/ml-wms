package com.wms.task.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.task.domain.entity.TaskHeader.Status;
import com.wms.task.domain.entity.TaskHeader.TaskType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskPageQuery extends PageRequest {
    private Long warehouseId;
    private Status status;
    private TaskType taskType;
}
