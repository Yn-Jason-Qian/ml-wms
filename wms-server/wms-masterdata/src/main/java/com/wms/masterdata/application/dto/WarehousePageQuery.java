package com.wms.masterdata.application.dto;

import com.wms.common.base.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WarehousePageQuery extends PageRequest {

    private String whCode;
    private String whName;
    private String whType;
    private Integer status;
}
