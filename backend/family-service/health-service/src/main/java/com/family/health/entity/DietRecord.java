package com.family.health.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DietRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long familyId;
    private String mealType; // breakfast lunch dinner snack
    private LocalDate recordDate;
    private LocalDateTime recordTime;
    private String foodName;
    private BigDecimal quantity;
    private String unit;
    private Integer calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private BigDecimal fiber;
    private String image;
    private String recognitionSource; // manual photo voice barcode
    private String aiAnalysis; // AI分析结果
}
