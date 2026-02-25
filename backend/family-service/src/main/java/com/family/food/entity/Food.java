package com.family.food.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 食物实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food")
public class Food extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 食物名称
     */
    private String name;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 每100克热量(卡路里)
     */
    private Integer calories;
    
    /**
     * 蛋白质(g)
     */
    private BigDecimal protein;
    
    /**
     * 碳水化合物(g)
     */
    private BigDecimal carbs;
    
    /**
     * 脂肪(g)
     */
    private BigDecimal fat;
    
    /**
     * 纤维(g)
     */
    private BigDecimal fiber;
    
    /**
     * 钠(mg)
     */
    private BigDecimal sodium;
    
    /**
     * 图片URL
     */
    private String image;
    
    /**
     * 来源：system-系统 food-用户自定义
     */
    private String source;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 状态 0禁用 1启用
     */
    private Integer status;
}
