package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.OrderHeader.OrderType;
import com.wms.outbound.domain.entity.OrderHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long warehouseId;
    private Long ownerId;
    private OrderType orderType;
    private String customerName;
    private String customerAddress;
    private Integer priority;
    private Status status;
    private LocalDateTime createdAt;
    private List<OrderLineDTO> lines;
}
