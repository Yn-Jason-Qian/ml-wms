package com.wms.outbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.domain.entity.ShipLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShipLineMapper extends BaseMapper<ShipLine> {}
