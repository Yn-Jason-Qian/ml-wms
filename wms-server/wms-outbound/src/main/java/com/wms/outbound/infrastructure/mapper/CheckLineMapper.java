package com.wms.outbound.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.outbound.domain.entity.CheckLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckLineMapper extends BaseMapper<CheckLine> {}
