package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Sku;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Select(
            "SELECT COUNT(*) > 0 FROM wms_masterdata_sku "
                    + "WHERE tenant_id = #{tenantId} AND sku_code = #{skuCode} AND is_deleted = 0 "
                    + "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(
            @Param("tenantId") Long tenantId,
            @Param("skuCode") String skuCode,
            @Param("excludeId") Long excludeId);
}
