package com.wms.outbound.application.dto;

import com.wms.outbound.domain.entity.PickHeader.PickType;
import com.wms.outbound.domain.entity.PickHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PickDTO {
    private Long id;
    private String pickNo;
    private Long warehouseId;
    private PickType pickType;
    private Status status;
    private Long assignTo;
    private LocalDateTime createdAt;

    /** 拣货行明细（仅详情接口返回，分页列表为空） */
    private List<PickLineDTO> lines;
}
