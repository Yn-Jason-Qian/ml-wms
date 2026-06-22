package com.wms.strategy.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_strategy_config")
public class StrategyConfig extends BaseEntity {

    private String strategyCode;
    private String strategyName;
    private String strategyType;
    private String description;
    private Integer sortOrder;
    private Integer isEnabled;

    /** 规则列表（仅内存中使用，不持久化） */
    @TableField(exist = false)
    private List<StrategyRule> rules;

    public static final String TYPE_PUTAWAY = "PUTAWAY";
    public static final String TYPE_ALLOCATION = "ALLOCATION";
    public static final String TYPE_WAVE = "WAVE";
    public static final String TYPE_PICKING = "PICKING";
    public static final String TYPE_REPLENISH = "REPLENISH";
}
