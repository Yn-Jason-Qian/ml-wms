package com.wms.outbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PickResultDTO {
    private String pickNo;
    private Long pickId;
}
