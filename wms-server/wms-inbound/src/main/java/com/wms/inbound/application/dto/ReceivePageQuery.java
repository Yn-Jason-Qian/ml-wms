package com.wms.inbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.inbound.domain.entity.ReceiveHeader.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReceivePageQuery extends PageRequest {
    private Long warehouseId;
    private Long ownerId;
    private String asnNo;
    private Status status;
}
