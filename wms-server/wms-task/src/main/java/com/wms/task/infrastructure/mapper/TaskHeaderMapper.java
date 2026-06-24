package com.wms.task.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.task.domain.entity.TaskHeader;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskHeaderMapper extends BaseMapper<TaskHeader> {}
