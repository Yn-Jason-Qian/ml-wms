package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_asn_header")
public class AsnHeader extends BaseEntity {

    /** ASN类型 */
    public enum AsnType {
        PURCHASE,
        RETURN,
        TRANSFER,
        ADJUST
    }

    /** ASN状态 */
    public enum Status {
        CREATED,
        RECEIVING,
        PARTIAL_RECEIVED,
        RECEIVED,
        CANCELLED,
        CLOSED
    }

    private Long warehouseId;
    private Long ownerId;
    private String asnNo;
    private AsnType asnType;
    private String sourceNo;
    private LocalDateTime expectedArriveTime;
    private String carrierName;
    private String carrierPhone;
    private Status status;
    private String remark;

    public void startReceiving() {
        this.status = Status.RECEIVING;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void close() {
        this.status = Status.CLOSED;
    }
}
