package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.OrderHeader.OrderType;
import com.wms.outbound.domain.entity.OrderHeader.Status;

import lombok.Data;

@Data
public class WaveOrderDTO {
    private Long id;
    private String orderNo;
    private OrderType orderType;
    private String customerName;
    private Integer priority;
    private Status status;
}
