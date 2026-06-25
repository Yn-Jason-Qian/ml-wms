package com.wms.task.application.assembler;

import com.wms.task.application.dto.TaskDTO;
import com.wms.task.domain.entity.TaskHeader;

import org.springframework.stereotype.Component;

@Component
public class TaskAssembler {

    public TaskDTO toTaskDTO(TaskHeader t) {
        TaskDTO d = new TaskDTO();
        d.setId(t.getId());
        d.setTaskNo(t.getTaskNo());
        d.setTaskType(t.getTaskType());
        d.setWarehouseId(t.getWarehouseId());
        d.setStatus(t.getStatus());
        d.setPriority(t.getPriority());
        d.setAssignTo(t.getAssignTo() != null ? t.getAssignTo() : 0L);
        d.setTaskSourceNo(t.getTaskSourceNo() != null ? t.getTaskSourceNo() : "");
        d.setCreatedAt(t.getCreatedAt());
        return d;
    }
}
