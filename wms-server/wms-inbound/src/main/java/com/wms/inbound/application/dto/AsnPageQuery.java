package com.wms.inbound.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsnPageQuery extends PageRequest {
    private Long warehouseId;
    private String status;
    private String asnNo;
}
