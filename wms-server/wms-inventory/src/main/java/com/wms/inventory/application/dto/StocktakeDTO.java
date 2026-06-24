package com.wms.inventory.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StocktakeDTO {
    private Long id;
    private Long warehouseId;
    private String stocktakeNo;
    private String stocktakeType;
    private String stocktakeMode;
    private String status;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalLines;
    private Integer diffLines;
    private LocalDateTime createdAt;
    private List<StocktakeLineDTO> lines;
}
