package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Unit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UnitMapper extends BaseMapper<Unit> {
}
