package com.wms.outbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.outbound.domain.entity.PickHeader.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PickPageQuery extends PageRequest {
    private Long warehouseId;
    private Status status;
}
