package com.family.food.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 食材实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Ingredient extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private String name;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private String storageLocation;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private Integer reminderDays;
    private Integer status; // 1正常 2快过期 3已过期
    private String image;
    private String recognizedData; // AI识别原始数据
}
