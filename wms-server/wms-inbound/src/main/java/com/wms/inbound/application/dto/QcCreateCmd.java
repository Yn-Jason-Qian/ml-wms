package com.wms.inbound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class QcCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long receiveHeaderId;
    @NotBlank private String qcType;
    private BigDecimal sampleRatio;
    private String remark;
}
