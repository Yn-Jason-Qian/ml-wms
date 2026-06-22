package com.wms.inbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_asn_header")
public class AsnHeader extends BaseEntity {
    private Long warehouseId;
    private Long ownerId;
    private String asnNo;
    private String asnType;
    private String sourceNo;
    private LocalDateTime expectedArriveTime;
    private String carrierName;
    private String carrierPhone;
    private String status;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RECEIVING = "RECEIVING";
    public static final String STATUS_PARTIAL_RECEIVED = "PARTIAL_RECEIVED";
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_CLOSED = "CLOSED";

    public void startReceiving() { this.status = STATUS_RECEIVING; }
    public void cancel() { this.status = STATUS_CANCELLED; }
    public void close() { this.status = STATUS_CLOSED; }
}
