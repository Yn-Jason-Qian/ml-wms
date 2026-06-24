package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_stock")
public class Stock extends BaseEntity {

    private Long warehouseId;
    private Long ownerId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String batchNo;
    private String lotAttrs;
    private LocalDateTime productionDate;
    private LocalDateTime expiryDate;
    private BigDecimal qtyOnHand;
    private BigDecimal qtyAllocated;
    private BigDecimal qtyAvailable;
    private BigDecimal qtyFrozen;
    private Long unitId;
    private LocalDateTime firstInTime;
    private LocalDateTime lastInTime;
    private Integer status;

    /** 冻结库存 */
    public void freeze(BigDecimal qty) {
        if (qty.compareTo(this.qtyAvailable) > 0) {
            throw new IllegalArgumentException("冻结数量不能超过可用数量");
        }
        this.qtyFrozen = this.qtyFrozen.add(qty);
        this.qtyAvailable = this.qtyAvailable.subtract(qty);
    }

    /** 解冻库存 */
    public void unfreeze(BigDecimal qty) {
        if (qty.compareTo(this.qtyFrozen) > 0) {
            throw new IllegalArgumentException("解冻数量不能超过已冻结数量");
        }
        this.qtyFrozen = this.qtyFrozen.subtract(qty);
        this.qtyAvailable = this.qtyAvailable.add(qty);
    }

    /** 分配库存（出库预留） */
    public void allocate(BigDecimal qty) {
        if (qty.compareTo(this.qtyAvailable) > 0) {
            throw new IllegalArgumentException("分配数量不能超过可用数量");
        }
        this.qtyAllocated = this.qtyAllocated.add(qty);
        this.qtyAvailable = this.qtyAvailable.subtract(qty);
    }

    /** 取消分配 */
    public void deallocate(BigDecimal qty) {
        if (qty.compareTo(this.qtyAllocated) > 0) {
            throw new IllegalArgumentException("取消分配数量不能超过已分配数量");
        }
        this.qtyAllocated = this.qtyAllocated.subtract(qty);
        this.qtyAvailable = this.qtyAvailable.add(qty);
    }

    /** 减少在手库存（出库确认/发货） */
    public void deduct(BigDecimal qty) {
        if (qty.compareTo(this.qtyOnHand) > 0) {
            throw new IllegalArgumentException("扣减数量不能超过在手数量");
        }
        this.qtyOnHand = this.qtyOnHand.subtract(qty);
        this.qtyAllocated = this.qtyAllocated.subtract(qty);
        // qtyAvailable 不变（已在 allocate 时扣减）
    }

    /** 增加在手库存（入库/上架） */
    public void add(BigDecimal qty) {
        this.qtyOnHand = this.qtyOnHand.add(qty);
        this.qtyAvailable = this.qtyAvailable.add(qty);
        this.lastInTime = LocalDateTime.now();
        if (this.firstInTime == null) {
            this.firstInTime = LocalDateTime.now();
        }
    }

    /** 库存是否为零 */
    public boolean isEmpty() {
        return qtyOnHand.compareTo(BigDecimal.ZERO) == 0;
    }
}
