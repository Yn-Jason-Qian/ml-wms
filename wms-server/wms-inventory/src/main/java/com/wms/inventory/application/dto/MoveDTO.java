package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.MoveHeader.MoveType;
import com.wms.inventory.domain.entity.MoveHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MoveDTO {
    private Long id;
    private Long warehouseId;
    private String moveNo;
    private MoveType moveType;
    private Status status;
    private String remark;
    private LocalDateTime createdAt;
    private List<MoveLineDTO> lines;
}
