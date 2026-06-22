package com.wms.outbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class WaveCreateCmd {
    @NotNull private Long warehouseId;
    @NotBlank private String waveType;
    @NotNull private List<Long> orderIds;
}
