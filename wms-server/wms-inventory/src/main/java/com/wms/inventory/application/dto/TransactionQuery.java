package com.wms.inventory.application.dto;

import com.wms.common.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionQuery extends PageRequest {
    private Long stockId;
    private Long skuId;
    private Long refId;
    private String txnType;
}
