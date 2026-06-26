package com.wms.task.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_task_header")
public class TaskHeader extends BaseEntity {

    /** 任务类型 */
    public enum TaskType {
        PUTAWAY,
        PICK,
        MOVE,
        REPLENISH,
        STOCKTAKE
    }

    /** 任务状态 */
    public enum Status {
        CREATED,
        RELEASED,
        ASSIGNED,
        EXECUTING,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private String taskNo;
    private TaskType taskType;
    private Long taskSourceId;
    private String taskSourceNo;
    private Integer totalLines;
    private Integer completedLines;
    private Integer priority;
    private Status status;
    private Long assignTo;
    private String assignRule;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deadline;
    private String remark;

    public void release() {
        this.status = Status.RELEASED;
    }

    public void assign(Long userId) {
        this.status = Status.ASSIGNED;
        this.assignTo = userId;
    }

    public void start() {
        this.status = Status.EXECUTING;
        this.startTime = LocalDateTime.now();
    }

    public void complete() {
        this.status = Status.DONE;
        this.endTime = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.endTime = LocalDateTime.now();
    }
}
