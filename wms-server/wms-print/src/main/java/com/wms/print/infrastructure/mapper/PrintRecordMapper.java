package com.wms.print.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.print.domain.entity.PrintRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrintRecordMapper extends BaseMapper<PrintRecord> {}
