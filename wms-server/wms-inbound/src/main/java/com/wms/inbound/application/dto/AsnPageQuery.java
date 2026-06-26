package com.wms.inbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.inbound.domain.entity.AsnHeader.AsnType;
import com.wms.inbound.domain.entity.AsnHeader.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsnPageQuery extends PageRequest {
    private Long warehouseId;
    private Long ownerId;
    private Status status;
    private String asnNo;
    private AsnType asnType;
}
