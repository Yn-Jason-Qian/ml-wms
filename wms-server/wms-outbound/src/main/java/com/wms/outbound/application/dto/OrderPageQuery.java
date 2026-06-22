package com.wms.outbound.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageQuery extends PageRequest {
    private Long warehouseId;
    private String status;
    private String orderType;
}
