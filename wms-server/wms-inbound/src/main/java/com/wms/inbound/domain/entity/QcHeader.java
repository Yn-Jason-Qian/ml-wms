package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_qc_header")
public class QcHeader extends BaseEntity {

    /** 质检类型 */
    public enum QcType {
        FULL,
        SAMPLE,
        NONE
    }

    /** 质检状态 */
    public enum Status {
        CREATED,
        QCING,
        PASS,
        REJECT
    }

    private Long warehouseId;
    private String qcNo;
    private Long receiveHeaderId;
    private QcType qcType;
    private BigDecimal sampleRatio;
    private Status status;
    private Long qcBy;
    private LocalDateTime qcAt;
    private String remark;
}
