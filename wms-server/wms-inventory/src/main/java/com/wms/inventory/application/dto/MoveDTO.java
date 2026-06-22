package com.wms.inventory.application.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MoveDTO {
    private Long id;
    private Long warehouseId;
    private String moveNo;
    private String moveType;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private List<MoveLineDTO> lines;
}
