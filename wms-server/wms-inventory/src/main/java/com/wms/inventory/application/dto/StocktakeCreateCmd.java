package com.wms.inventory.application.dto;

import com.wms.inventory.domain.entity.StocktakeHeader.StocktakeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StocktakeCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private StocktakeType stocktakeType;
    @NotBlank private String stocktakeMode;
    private String locationRange;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
}
