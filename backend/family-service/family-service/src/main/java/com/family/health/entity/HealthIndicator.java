package com.family.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 健康指标实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_indicator")
public class HealthIndicator extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 指标类型：weight-体重 height-身高 bmi-BMI temperature-体温 
     * blood_pressure-血压 heart_rate-心率 blood_sugar-血糖
     */
    private String indicatorType;
    
    /**
     * 指标值
     */
    private BigDecimal indicatorValue;
    
    /**
     * 指标单位
     */
    private String unit;
    
    /**
     * 记录日期
     */
    private LocalDate recordDate;
    
    /**
     * 指标状态：normal-正常 abnormal-异常 warning-警告
     */
    private String indicatorStatus;
    
    /**
     * 参考最小值
     */
    private BigDecimal referenceMin;
    
    /**
     * 参考最大值
     */
    private BigDecimal referenceMax;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 状态 0删除 1正常
     */
    private Integer status;
}
