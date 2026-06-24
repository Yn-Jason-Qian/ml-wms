package com.wms.outbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.domain.entity.ShipHeader;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShipHeaderMapper extends BaseMapper<ShipHeader> {}
