package com.wms.inbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inbound_putaway_header")
public class PutawayHeader extends BaseEntity {
    private Long warehouseId;
    private String putawayNo;
    private Long receiveHeaderId;
    private String status;
    private Long strategyId;
    private String remark;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PUTAWAYING = "PUTAWAYING";
    public static final String STATUS_PARTIAL_DONE = "PARTIAL_DONE";
    public static final String STATUS_DONE = "DONE";
}
