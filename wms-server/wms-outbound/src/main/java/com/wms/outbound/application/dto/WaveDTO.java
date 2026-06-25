package com.wms.outbound.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WaveDTO {
    private Long id;
    private String waveNo;
    private Long warehouseId;
    private String waveType;
    private String waveStatus;
    private Integer orderCount;
    private LocalDateTime createdAt;
    private List<WaveOrderDTO> orders;
}
