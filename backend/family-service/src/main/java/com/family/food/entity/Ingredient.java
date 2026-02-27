package com.family.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 食材实体
 */
@Data
@TableName("ingredient")
public class Ingredient {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private String name;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private String storageLocation;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private Integer reminderDays;
    private Integer status;
    private String image;
    private String recognizedData;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
