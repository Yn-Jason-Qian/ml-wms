package com.wms.inbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PutawayResultDTO {
    private String putawayNo;
    private Long putawayHeaderId;
}
