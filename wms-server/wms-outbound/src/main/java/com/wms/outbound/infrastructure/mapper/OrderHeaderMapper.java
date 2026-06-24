package com.wms.outbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.domain.entity.OrderHeader;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderHeaderMapper extends BaseMapper<OrderHeader> {}
