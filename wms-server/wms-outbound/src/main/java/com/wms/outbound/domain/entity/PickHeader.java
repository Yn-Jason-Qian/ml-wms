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
    private Long warehouseId;
    private String pickNo;
    private Long waveHeaderId;
    private String pickType;
    private String pickZone;
    private Long assignTo;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_PICKING = "PICKING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_DONE = "DONE";
}
