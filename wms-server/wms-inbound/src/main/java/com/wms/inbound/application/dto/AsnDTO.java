package com.wms.inbound.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsnDTO {
    private Long id;
    private Long warehouseId;
    private Long ownerId;
    private String asnNo;
    private String asnType;
    private String sourceNo;
    private LocalDateTime expectedArriveTime;
    private String carrierName;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private List<AsnLineDTO> lines;
}
