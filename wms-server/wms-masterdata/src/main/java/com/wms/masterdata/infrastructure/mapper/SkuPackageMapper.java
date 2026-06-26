package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.SkuPackage;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SkuPackageMapper extends BaseMapper<SkuPackage> {

    @Select(
            "SELECT COUNT(*) > 0 FROM wms_masterdata_sku_package "
                    + "WHERE tenant_id = #{tenantId} AND sku_id = #{skuId} "
                    + "AND package_level = #{packageLevel} AND is_deleted = 0 "
                    + "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByLevel(
            @Param("tenantId") Long tenantId,
            @Param("skuId") Long skuId,
            @Param("packageLevel") SkuPackage.PackageLevel packageLevel,
            @Param("excludeId") Long excludeId);
}
