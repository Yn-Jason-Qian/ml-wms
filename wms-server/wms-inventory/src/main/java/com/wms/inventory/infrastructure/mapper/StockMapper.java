package com.wms.inventory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.inventory.domain.entity.Stock;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /** 乐观锁更新（version 自增，仅版本匹配时更新成功） */
    @Update(
            "UPDATE wms_inventory_stock SET qty_on_hand = #{qtyOnHand}, "
                    + "qty_allocated = #{qtyAllocated}, qty_available = #{qtyAvailable}, "
                    + "qty_frozen = #{qtyFrozen}, last_in_time = #{lastInTime}, "
                    + "updated_at = NOW(), version = version + 1 "
                    + "WHERE id = #{id} AND version = #{version}")
    int updateWithVersion(Stock stock);

    /** 按唯一键查找库存 */
    @org.apache.ibatis.annotations.Select(
            "SELECT * FROM wms_inventory_stock "
                    + "WHERE tenant_id = #{tenantId} AND warehouse_id = #{warehouseId} "
                    + "AND location_id = #{locationId} AND sku_id = #{skuId} "
                    + "AND IFNULL(batch_no,'') = IFNULL(#{batchNo},'') AND is_deleted = 0")
    Stock findByKey(
            @Param("tenantId") Long tenantId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId,
            @Param("skuId") Long skuId,
            @Param("batchNo") String batchNo);
}
