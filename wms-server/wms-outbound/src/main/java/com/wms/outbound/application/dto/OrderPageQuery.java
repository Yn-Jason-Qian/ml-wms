package com.wms.outbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.outbound.domain.entity.OrderHeader.OrderType;
import com.wms.outbound.domain.entity.OrderHeader.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageQuery extends PageRequest {
    private Long warehouseId;
    private Status status;
    private OrderType orderType;
}
