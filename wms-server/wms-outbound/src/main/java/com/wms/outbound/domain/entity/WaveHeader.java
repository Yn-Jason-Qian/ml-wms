package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_wave_header")
public class WaveHeader extends BaseEntity {
    private Long warehouseId;
    private String waveNo;
    private String waveType;
    private String waveStatus;
    private Integer orderCount;
    private Integer totalLines;
    private BigDecimal totalQty;
    private Long strategyId;
    private Long releasedBy;
    private LocalDateTime releasedAt;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_PICKING = "PICKING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_DONE = "DONE";

    public void release() {
        this.waveStatus = STATUS_RELEASED;
        this.releasedAt = LocalDateTime.now();
    }
}
