package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_inventory_stocktake_header")
public class StocktakeHeader extends BaseEntity {

    /** 盘点类型 */
    public enum StocktakeType {
        FULL,
        AREA,
        LOCATION,
        SKU,
        BLIND
    }

    /** 盘点状态 */
    public enum Status {
        CREATED,
        COUNTING,
        COUNTED,
        DIFF_REVIEW,
        ADJUSTED,
        DONE,
        CANCELLED
    }

    private Long warehouseId;
    private String stocktakeNo;
    private StocktakeType stocktakeType;
    private String stocktakeMode;
    private String locationRange;
    private Status status;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalLines;
    private Integer firstCountLines;
    private Integer secondCountLines;
    private Integer diffLines;

    public void start() {
        this.status = Status.COUNTING;
        this.startTime = LocalDateTime.now();
    }

    public void finish() {
        this.status = Status.COUNTED;
        this.endTime = LocalDateTime.now();
    }

    public void reviewDiffs() {
        this.status = Status.DIFF_REVIEW;
    }

    public void adjust() {
        this.status = Status.ADJUSTED;
    }

    public void done() {
        this.status = Status.DONE;
    }
}
