package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_pick_header")
public class PickHeader extends BaseEntity {

    /** 拣货类型 */
    public enum PickType {
        PIECE,
        PALLET,
        PAPER,
        RF
    }

    /** 拣货状态 */
    public enum Status {
        CREATED,
        ASSIGNED,
        PICKING,
        PICKED,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private String pickNo;
    private Long waveHeaderId;
    private PickType pickType;
    private String pickZone;
    private Long assignTo;
    private Status status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
