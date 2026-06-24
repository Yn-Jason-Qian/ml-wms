package com.wms.masterdata.application.dto;

import com.wms.common.base.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OwnerPageQuery extends PageRequest {
    private String ownerCode;
    private String ownerName;
}
