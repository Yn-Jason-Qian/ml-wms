package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LocationMapper extends BaseMapper<Location> {

    @Select("SELECT COUNT(*) > 0 FROM wms_masterdata_location " +
            "WHERE tenant_id = #{tenantId} AND warehouse_id = #{warehouseId} " +
            "AND location_code = #{locationCode} AND is_deleted = 0 " +
            "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(@Param("tenantId")Long tenantId,
                         @Param("warehouseId")Long warehouseId,
                         @Param("locationCode")String locationCode,
                         @Param("excludeId")Long excludeId);
}
