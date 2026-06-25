package com.wms.outbound.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickDTO {
    private Long id;
    private String pickNo;
    private Long warehouseId;
    private String pickType;
    private String status;
    private Long assignTo;
    private LocalDateTime createdAt;
}
