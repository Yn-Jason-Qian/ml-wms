package com.wms.outbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/** PDA 拣货确认 */
@Data
public class PickSubmitCmd {
    @NotNull private Long pickHeaderId;
    @NotNull private Long pickLineId;
    @NotNull private BigDecimal pickedQty;
    @NotBlank private String toContainer;
}
