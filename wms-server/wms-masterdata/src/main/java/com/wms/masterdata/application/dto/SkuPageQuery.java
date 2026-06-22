package com.wms.masterdata.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkuPageQuery extends PageRequest {
    private Long ownerId;
    private String skuCode;
    private String skuName;
    private String category;
    private Integer batchManaged;
}
