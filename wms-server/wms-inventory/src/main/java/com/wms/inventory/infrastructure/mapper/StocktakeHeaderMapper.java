package com.wms.inventory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inventory.domain.entity.StocktakeHeader;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StocktakeHeaderMapper extends BaseMapper<StocktakeHeader> {}
