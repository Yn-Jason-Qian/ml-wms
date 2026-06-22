package com.wms.strategy.domain.entity;

import com.wms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_strategy_rule")
public class StrategyRule extends BaseEntity {

    private Long strategyId;
    private Integer ruleNo;
    private String ruleName;
    private String conditionsJson;
    private String actionsJson;
    private Integer isEnabled;
}
