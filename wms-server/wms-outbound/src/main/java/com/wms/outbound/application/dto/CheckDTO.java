package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.CheckHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckDTO {
    private Long id;
    private String checkNo;
    private Long warehouseId;
    private Long waveHeaderId;
    private Status status;
    private Long checkBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    /** 复核行明细（仅详情接口返回，分页列表为空） */
    private List<CheckLineDTO> lines;
}
