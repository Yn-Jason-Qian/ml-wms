package com.wms.masterdata.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationPageQuery extends PageRequest {
    private Long warehouseId;
    private Long areaId;
    private String locationCode;
    private String locationType;
    private Integer status;
}
