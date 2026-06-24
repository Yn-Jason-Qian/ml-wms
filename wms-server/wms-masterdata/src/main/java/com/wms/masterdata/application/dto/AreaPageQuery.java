package com.wms.masterdata.application.dto;

import com.wms.common.base.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AreaPageQuery extends PageRequest {
    private Long warehouseId;
    private String areaCode;
    private String areaName;
    private String areaType;
}
