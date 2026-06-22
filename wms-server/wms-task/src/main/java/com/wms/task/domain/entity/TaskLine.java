package com.wms.task.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_task_line")
public class TaskLine extends BaseEntity {
    private Long taskHeaderId;
    private Integer lineNo;
    private Long refId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal taskQty;
    private BigDecimal doneQty;
    private Long fromLocationId;
    private Long toLocationId;
    private String batchNo;
    private String lotAttrs;
    private String containerCode;
    private String status;
    private Long execBy;
    private LocalDateTime execStartTime;
    private LocalDateTime execEndTime;
}
