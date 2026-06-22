package com.wms.inventory.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StocktakePageQuery extends PageRequest {
    private Long warehouseId;
    private String status;
}
