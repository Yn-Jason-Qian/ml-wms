package com.wms.outbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResultDTO {
    private String orderNo;
    private Long orderId;
}
