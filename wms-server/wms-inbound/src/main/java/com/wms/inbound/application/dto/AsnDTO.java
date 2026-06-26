package com.wms.inbound.application.dto;

import com.wms.inbound.domain.entity.AsnHeader.AsnType;
import com.wms.inbound.domain.entity.AsnHeader.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsnDTO {
    private Long id;
    private Long warehouseId;
    private Long ownerId;
    private String asnNo;
    private AsnType asnType;
    private String sourceNo;
    private LocalDateTime expectedArriveTime;
    private String carrierName;
    private Status status;
    private String remark;
    private LocalDateTime createdAt;
    private List<AsnLineDTO> lines;
}
