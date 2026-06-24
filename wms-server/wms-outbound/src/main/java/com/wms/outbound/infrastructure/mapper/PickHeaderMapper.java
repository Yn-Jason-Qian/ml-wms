package com.wms.outbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.domain.entity.PickHeader;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PickHeaderMapper extends BaseMapper<PickHeader> {}
