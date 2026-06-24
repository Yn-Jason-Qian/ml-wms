package com.wms.inbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inbound.domain.entity.AsnLine;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AsnLineMapper extends BaseMapper<AsnLine> {}
