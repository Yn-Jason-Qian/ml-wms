package com.wms.inbound.application.dto;

import com.wms.inbound.domain.entity.ReceiveHeader.ReceiveType;
import com.wms.inbound.domain.entity.ReceiveHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceiveDTO {
    private Long id;
    private String receiveNo;
    private Long warehouseId;
    private Long ownerId;
    private Long asnHeaderId;
    private String asnNo;
    private ReceiveType receiveType;
    private Status status;
    private String remark;
    private Long receivedBy;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
    private List<ReceiveLineDTO> lines;
}
