package com.wms.inbound.application.dto;

import com.wms.inbound.domain.entity.QcHeader.QcType;
import com.wms.inbound.domain.entity.QcHeader.Status;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QcDTO {
    private Long id;
    private String qcNo;
    private Long warehouseId;
    private Long receiveHeaderId;
    private String receiveNo;
    private Long ownerId;
    private QcType qcType;
    private BigDecimal sampleRatio;
    private Status status;
    private String remark;
    private Long qcBy;
    private LocalDateTime qcAt;
    private LocalDateTime createdAt;
    private List<QcLineDTO> lines;
}
