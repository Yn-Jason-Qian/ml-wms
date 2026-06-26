package com.wms.inbound.application.dto;

import com.wms.inbound.domain.entity.PutawayHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PutawayDTO {
    private Long id;
    private String putawayNo;
    private Long warehouseId;
    private Long receiveHeaderId;
    private String receiveNo;
    private Long ownerId;
    private Status status;
    private Long strategyId;
    private String remark;
    private LocalDateTime createdAt;
    private List<PutawayLineDTO> lines;
}
