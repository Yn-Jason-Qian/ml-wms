package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_check_header")
public class CheckHeader extends BaseEntity {

    /** 复核状态 */
    public enum Status {
        CREATED,
        CHECKING,
        PASS,
        REJECT,
        DONE
    }

    private Long warehouseId;
    private String checkNo;
    private Long waveHeaderId;
    private Status status;
    private Long checkBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
}
