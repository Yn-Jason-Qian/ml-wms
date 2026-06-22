package com.wms.inbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_receive_header")
public class ReceiveHeader extends BaseEntity {
    private Long warehouseId;
    private Long ownerId;
    private String receiveNo;
    private Long asnHeaderId;
    private String receiveType;
    private String status;
    private Long receivedBy;
    private LocalDateTime receivedAt;
    private String remark;

    public static final String TYPE_ASN = "ASN";
    public static final String TYPE_BLIND = "BLIND";
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RECEIVING = "RECEIVING";
    public static final String STATUS_DONE = "DONE";
}
