package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PutawayCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long receiveHeaderId;
}
