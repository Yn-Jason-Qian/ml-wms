package com.wms.inbound.application.dto;

import com.wms.common.base.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PutawayPageQuery extends PageRequest {
    private Long warehouseId;
    private String status;
}
