package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AreaMapper extends BaseMapper<Area> {

    @Select("SELECT COUNT(*) > 0 FROM wms_masterdata_area " +
            "WHERE tenant_id = #{tenantId} AND warehouse_id = #{warehouseId} " +
            "AND area_code = #{areaCode} AND is_deleted = 0 " +
            "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(@Param("tenantId")Long tenantId,
                         @Param("warehouseId")Long warehouseId,
                         @Param("areaCode")String areaCode,
                         @Param("excludeId")Long excludeId);
}
