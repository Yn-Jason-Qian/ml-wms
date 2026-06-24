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

    private Long warehouseId;
    private String stocktakeNo;
    private String stocktakeType;
    private String stocktakeMode;
    private String locationRange;
    private String status;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalLines;
    private Integer firstCountLines;
    private Integer secondCountLines;
    private Integer diffLines;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_COUNTING = "COUNTING";
    public static final String STATUS_COUNTED = "COUNTED";
    public static final String STATUS_DIFF_REVIEW = "DIFF_REVIEW";
    public static final String STATUS_ADJUSTED = "ADJUSTED";
    public static final String STATUS_DONE = "DONE";

    public void start() {
        this.status = STATUS_COUNTING;
        this.startTime = LocalDateTime.now();
    }

    public void finish() {
        this.status = STATUS_COUNTED;
        this.endTime = LocalDateTime.now();
    }

    public void reviewDiffs() {
        this.status = STATUS_DIFF_REVIEW;
    }

    public void adjust() {
        this.status = STATUS_ADJUSTED;
    }

    public void done() {
        this.status = STATUS_DONE;
    }
}
