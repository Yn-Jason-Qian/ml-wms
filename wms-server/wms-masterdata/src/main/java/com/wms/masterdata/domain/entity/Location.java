package com.wms.masterdata.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_masterdata_location")
public class Location extends BaseEntity {

    private Long warehouseId;
    private Long areaId;
    private String locationCode;
    private String locationName;
    private String locationType;
    private String aisle;
    private String shelf;
    private String tier;
    private String depthPos;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private Integer maxQty;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String roadway;
    private Integer status;

    public static final String TYPE_FLOOR = "FLOOR";
    public static final String TYPE_RACK = "RACK";
    public static final String TYPE_SHELF = "SHELF";
    public static final String TYPE_BIN = "BIN";

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_IDLE = 1;
    public static final int STATUS_OCCUPIED = 2;

    public void disable() { this.status = STATUS_DISABLED; }
    public void enable() { this.status = STATUS_IDLE; }
    public boolean isEnabled() { return status != null && status != STATUS_DISABLED; }

    /** 按规则自动生成库位编码：WH-01-01-01-01 (巷道-货架-层-位) */
    public static String generateCode(String warehousePrefix, String aisle, String shelf,
String tier, String depth) {
        return String.format("%s-%s-%s-%s-%s", warehousePrefix, aisle, shelf, tier, depth);
    }
}
