package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** PDA 上架确认 */
@Data
public class PutawaySubmitCmd {
    @NotNull private Long putawayHeaderId;
    @NotNull private Long putawayLineId;
    private Long toLocationId;
}
