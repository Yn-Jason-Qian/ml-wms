package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Warehouse;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    @Select(
            "SELECT COUNT(*) > 0 FROM wms_masterdata_warehouse "
                    + "WHERE tenant_id = #{tenantId} AND wh_code = #{whCode} AND is_deleted = 0 "
                    + "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(
            @Param("tenantId") Long tenantId,
            @Param("whCode") String whCode,
            @Param("excludeId") Long excludeId);
}
