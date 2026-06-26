package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.PickHeader.PickType;
import com.wms.outbound.domain.entity.PickHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickDTO {
    private Long id;
    private String pickNo;
    private Long warehouseId;
    private PickType pickType;
    private Status status;
    private Long assignTo;
    private LocalDateTime createdAt;
}
