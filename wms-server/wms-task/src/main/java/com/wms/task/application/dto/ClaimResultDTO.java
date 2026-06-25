package com.wms.task.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClaimResultDTO {
    private Long taskId;
    private String status;
}
