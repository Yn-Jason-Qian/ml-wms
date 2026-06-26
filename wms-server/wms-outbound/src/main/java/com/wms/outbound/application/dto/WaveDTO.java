package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.WaveHeader.WaveStatus;
import com.wms.outbound.domain.entity.WaveHeader.WaveType;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WaveDTO {
    private Long id;
    private String waveNo;
    private Long warehouseId;
    private WaveType waveType;
    private WaveStatus waveStatus;
    private Integer orderCount;
    private LocalDateTime createdAt;
    private List<WaveOrderDTO> orders;
}
