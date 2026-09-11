package com.wms.outbound.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckResultDTO {
    private String checkNo;
    private Long checkId;
}
