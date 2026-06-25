package com.wms.outbound.application.dto;

import lombok.Data;

@Data
public class WaveOrderDTO {
    private Long id;
    private String orderNo;
    private String orderType;
    private String customerName;
    private Integer priority;
    private String status;
}
