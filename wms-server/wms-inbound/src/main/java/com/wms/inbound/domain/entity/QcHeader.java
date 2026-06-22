package com.wms.inbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_qc_header")
public class QcHeader extends BaseEntity {
    private Long warehouseId;
    private String qcNo;
    private Long receiveHeaderId;
    private String qcType;
    private BigDecimal sampleRatio;
    private String status;
    private Long qcBy;
    private LocalDateTime qcAt;
    private String remark;

    public static final String TYPE_FULL = "FULL";
    public static final String TYPE_SAMPLE = "SAMPLE";
    public static final String TYPE_NONE = "NONE";
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_QCING = "QCING";
    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_REJECT = "REJECT";
}
