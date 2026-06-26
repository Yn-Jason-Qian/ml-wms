package com.wms.inbound.application.dto;

import com.wms.inbound.domain.entity.QcHeader.QcType;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QcCreateCmd {
    @NotNull private Long warehouseId;
    @NotNull private Long receiveHeaderId;
    @NotNull private QcType qcType;
    private BigDecimal sampleRatio;
    private String remark;
}
