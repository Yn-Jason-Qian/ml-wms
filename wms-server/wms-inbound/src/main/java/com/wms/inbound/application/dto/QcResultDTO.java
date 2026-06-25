package com.wms.inbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QcResultDTO {
    private String qcNo;
    private Long qcHeaderId;
}
