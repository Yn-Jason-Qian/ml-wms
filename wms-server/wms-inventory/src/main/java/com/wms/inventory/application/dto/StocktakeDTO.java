package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.StocktakeHeader.Status;
import com.wms.inventory.domain.entity.StocktakeHeader.StocktakeType;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StocktakeDTO {
    private Long id;
    private Long warehouseId;
    private String stocktakeNo;
    private StocktakeType stocktakeType;
    private String stocktakeMode;
    private Status status;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalLines;
    private Integer diffLines;
    private LocalDateTime createdAt;
    private List<StocktakeLineDTO> lines;
}
