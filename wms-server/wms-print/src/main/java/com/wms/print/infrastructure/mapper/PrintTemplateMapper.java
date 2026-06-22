package com.wms.print.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.print.domain.entity.PrintTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrintTemplateMapper extends BaseMapper<PrintTemplate> {}
