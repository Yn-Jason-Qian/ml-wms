package com.wms.task.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_task_header")
public class TaskHeader extends BaseEntity {
    private Long warehouseId;
    private String taskNo;
    private String taskType;
    private Long taskSourceId;
    private String taskSourceNo;
    private Integer totalLines;
    private Integer completedLines;
    private Integer priority;
    private String status;
    private Long assignTo;
    private String assignRule;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deadline;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_EXECUTING = "EXECUTING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public void release() { this.status = STATUS_RELEASED; }
    public void assign(Long userId) { this.status = STATUS_ASSIGNED; this.assignTo = userId; }
    public void start() { this.status = STATUS_EXECUTING; this.startTime = LocalDateTime.now(); }
    public void complete() { this.status = STATUS_DONE; this.endTime = LocalDateTime.now(); }
}
