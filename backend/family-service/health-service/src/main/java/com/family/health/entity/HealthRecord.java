package com.family.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 健康记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_record")
public class HealthRecord extends BaseEntity {
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
     * 记录日期
     */
    private LocalDate recordDate;
    
    /**
     * 体重kg
     */
    private BigDecimal weight;
    
    /**
     * 身高cm
     */
    private BigDecimal height;
    
    /**
     * BMI指数
     */
    private BigDecimal bmi;
    
    /**
     * 体温
     */
    private BigDecimal temperature;
    
    /**
     * 血压-收缩压
     */
    private Integer bloodPressureSystolic;
    
    /**
     * 血压-舒张压
     */
    private Integer bloodPressureDiastolic;
    
    /**
     * 心率
     */
    private Integer heartRate;
    
    /**
     * 血糖
     */
    private BigDecimal bloodSugar;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 状态 0删除 1正常
     */
    private Integer status;
}
