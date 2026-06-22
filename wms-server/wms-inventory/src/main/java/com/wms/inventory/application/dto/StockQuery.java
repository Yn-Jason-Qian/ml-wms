package com.wms.inventory.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuery extends PageRequest {
    private Long warehouseId;
    private Long ownerId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String batchNo;
    private Integer status;
    /** 仅查有库存的 */
    private Boolean onlyAvailable;
    /** 效期预警天数（查 N 天内过期） */
    private Integer expiryWithinDays;
}
