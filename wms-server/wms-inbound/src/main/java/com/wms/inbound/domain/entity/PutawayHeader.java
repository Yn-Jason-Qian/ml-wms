package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_putaway_header")
public class PutawayHeader extends BaseEntity {

    /** 上架状态 */
    public enum Status {
        CREATED,
        PUTAWAYING,
        PARTIAL_DONE,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private String putawayNo;
    private Long receiveHeaderId;
    private Status status;
    private Long strategyId;
    private String remark;
}
