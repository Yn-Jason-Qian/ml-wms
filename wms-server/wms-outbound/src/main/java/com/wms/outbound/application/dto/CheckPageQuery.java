package com.wms.outbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.outbound.domain.entity.CheckHeader.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CheckPageQuery extends PageRequest {
    private Long warehouseId;
    private Status status;
}
