package com.wms.outbound.application.dto;

import com.wms.common.base.PageRequest;
import com.wms.outbound.domain.entity.WaveHeader.WaveStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WavePageQuery extends PageRequest {
    private Long warehouseId;
    private WaveStatus waveStatus;
}
