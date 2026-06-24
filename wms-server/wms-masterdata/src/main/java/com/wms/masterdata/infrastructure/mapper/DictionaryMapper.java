package com.wms.masterdata.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.masterdata.domain.entity.Dictionary;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    @Select(
            "SELECT DISTINCT dict_type FROM wms_masterdata_dictionary "
                    + "WHERE tenant_id = #{tenantId} AND is_deleted = 0 ORDER BY dict_type")
    List<String> findAllTypes(@Param("tenantId") Long tenantId);

    @Select(
            "SELECT COUNT(*) > 0 FROM wms_masterdata_dictionary "
                    + "WHERE tenant_id = #{tenantId} AND dict_type = #{dictType} "
                    + "AND dict_code = #{dictCode} AND is_deleted = 0 "
                    + "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    boolean existsByCode(
            @Param("tenantId") Long tenantId,
            @Param("dictType") String dictType,
            @Param("dictCode") String dictCode,
            @Param("excludeId") Long excludeId);
}
