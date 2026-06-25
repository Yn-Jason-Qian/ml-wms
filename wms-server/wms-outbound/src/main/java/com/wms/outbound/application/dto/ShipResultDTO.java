package com.wms.outbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShipResultDTO {
    private String shipNo;
    private Long shipId;
}
