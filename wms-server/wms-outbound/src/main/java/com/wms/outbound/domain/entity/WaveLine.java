package com.wms.outbound.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_outbound_wave_line")
public class WaveLine extends BaseEntity {
    private Long waveHeaderId;
    private Long orderHeaderId;
    private Integer sortOrder;
}
