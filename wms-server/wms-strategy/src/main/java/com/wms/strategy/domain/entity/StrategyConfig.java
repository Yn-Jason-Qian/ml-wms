package com.wms.strategy.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_strategy_config")
public class StrategyConfig extends BaseEntity {

    /** 策略类型 */
    public enum StrategyType {
        PUTAWAY,
        ALLOCATION,
        WAVE,
        PICKING,
        REPLENISH
    }

    private String strategyCode;
    private String strategyName;
    private StrategyType strategyType;
    private String description;
    private Integer sortOrder;
    private Integer isEnabled;

    /** 规则列表（仅内存中使用，不持久化） */
    @TableField(exist = false)
    private List<StrategyRule> rules;
}
