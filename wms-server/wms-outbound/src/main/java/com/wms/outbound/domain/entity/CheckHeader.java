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
    private Long warehouseId;
    private String checkNo;
    private Long waveHeaderId;
    private String status;
    private Long checkBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_CHECKING = "CHECKING";
    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_REJECT = "REJECT";
}
