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

    /** 波次类型 */
    public enum WaveType {
        ORDER_POOL,
        PICK_ZONE,
        PRIORITY,
        TIME_WINDOW
    }

    /** 波次状态 */
    public enum WaveStatus {
        CREATED,
        RELEASED,
        PICKING,
        PICKED,
        DONE
    }

    private Long warehouseId;
    private String waveNo;
    private WaveType waveType;
    private WaveStatus waveStatus;
    private Integer orderCount;
    private Integer totalLines;
    private BigDecimal totalQty;
    private Long strategyId;
    private Long releasedBy;
    private LocalDateTime releasedAt;

    public void release() {
        this.waveStatus = WaveStatus.RELEASED;
        this.releasedAt = LocalDateTime.now();
    }
}
