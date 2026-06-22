package com.wms.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StocktakeCreateCmd {
    @NotNull private Long warehouseId;
    @NotBlank private String stocktakeType;
    @NotBlank private String stocktakeMode;
    private String locationRange;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
}
