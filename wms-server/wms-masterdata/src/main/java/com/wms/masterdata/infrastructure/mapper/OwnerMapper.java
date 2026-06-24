package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Owner;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OwnerMapper extends BaseMapper<Owner> {

    @Select(
            "SELECT COUNT(*) > 0 FROM wms_masterdata_owner "
                    + "WHERE tenant_id = #{tenantId} AND owner_code = #{ownerCode} AND is_deleted = 0 "
                    + "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(
            @Param("tenantId") Long tenantId,
            @Param("ownerCode") String ownerCode,
            @Param("excludeId") Long excludeId);
}
