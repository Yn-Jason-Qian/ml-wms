package com.wms.inbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inbound.domain.entity.AsnHeader;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AsnHeaderMapper extends BaseMapper<AsnHeader> {
}
