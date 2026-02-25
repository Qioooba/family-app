package com.family.health.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 营养分析响应DTO
 */
@Data
public class NutritionResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分析日期
     */
    private String date;
    
    /**
     * 总卡路里
     */
    private BigDecimal totalCalories;
    
    /**
     * 目标卡路里
     */
    private BigDecimal targetCalories;
    
    /**
     * 卡路里完成百分比
     */
    private Integer caloriesPercent;
    
    /**
     * 蛋白质(克)
     */
    private BigDecimal protein;
    
    /**
     * 碳水化合物(克)
     */
    private BigDecimal carbohydrates;
    
    /**
     * 脂肪(克)
     */
    private BigDecimal fat;
    
    /**
     * 膳食纤维(克)
     */
    private BigDecimal fiber;
    
    /**
     * 钠(毫克)
     */
    private BigDecimal sodium;
    
    /**
     * 糖(克)
     */
    private BigDecimal sugar;
    
    /**
     * 各营养素占比
     */
    private Map<String, BigDecimal> nutrientRatio;
    
    /**
     * 营养评分: A-优秀, B-良好, C-一般, D-需改善
     */
    private String grade;
    
    /**
     * 营养建议
     */
    private List<String> suggestions;
    
    /**
     * 餐次分布
     */
    private List<MealDistribution> mealDistribution;
    
    /**
     * 餐次分布
     */
    @Data
    public static class MealDistribution {
        private String mealType; // breakfast, lunch, dinner, snack
        private String mealName;
        private BigDecimal calories;
        private BigDecimal percent;
    }
}
