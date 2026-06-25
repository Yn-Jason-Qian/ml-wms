package com.wms.inbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiveResultDTO {
    private String receiveNo;
    private Long receiveHeaderId;
}
