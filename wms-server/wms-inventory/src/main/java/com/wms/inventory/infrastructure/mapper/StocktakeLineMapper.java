package com.wms.inventory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inventory.domain.entity.StocktakeLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StocktakeLineMapper extends BaseMapper<StocktakeLine> {
}
