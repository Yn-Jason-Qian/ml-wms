package com.wms.common.event;

import lombok.Getter;

import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

/** 任务状态变更事件 — 驱动 WebSocket 实时推送 放在 wms-common 以便所有模块都能访问 */
@Getter
public class TaskEvent extends ApplicationEvent {

    private final String eventType;
    private final Long taskId;
    private final String taskNo;
    private final String taskType;
    private final Long warehouseId;
    private final Long assignTo;

    public TaskEvent(
            Object source,
            String eventType,
            Long taskId,
            String taskNo,
            String taskType,
            Long warehouseId,
            Long assignTo) {
        super(source);
        this.eventType = eventType;
        this.taskId = taskId;
        this.taskNo = taskNo;
        this.taskType = taskType;
        this.warehouseId = warehouseId;
        this.assignTo = assignTo;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("eventType", eventType);
        m.put("taskId", taskId);
        m.put("taskNo", taskNo != null ? taskNo : "");
        m.put("taskType", taskType != null ? taskType : "");
        m.put("warehouseId", warehouseId != null ? warehouseId : 0);
        m.put("assignTo", assignTo != null ? assignTo : 0);
        m.put("timestamp", System.currentTimeMillis());
        return m;
    }
}
