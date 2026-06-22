package com.wms.task.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.task.domain.entity.TaskLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskLineMapper extends BaseMapper<TaskLine> {}
