package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_receive_header")
public class ReceiveHeader extends BaseEntity {

    /** 收货类型 */
    public enum ReceiveType {
        ASN,
        BLIND
    }

    /** 收货状态 */
    public enum Status {
        CREATED,
        RECEIVING,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private Long ownerId;
    private String receiveNo;
    private Long asnHeaderId;
    private ReceiveType receiveType;
    private Status status;
    private Long receivedBy;
    private LocalDateTime receivedAt;
    private String remark;
}
