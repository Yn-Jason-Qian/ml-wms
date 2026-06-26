package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.WaveHeader.WaveType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

@Data
public class WaveCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private WaveType waveType;
    @NotNull private List<Long> orderIds;
}
