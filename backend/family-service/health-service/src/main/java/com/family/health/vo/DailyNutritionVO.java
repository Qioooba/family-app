package com.family.health.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 每日营养分析VO
 */
@Data
public class DailyNutritionVO {
    
    private Long userId;
    private String date;
    
    // 总摄入量
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    
    // 目标值
    private BigDecimal targetCalories;
    private BigDecimal targetProtein;
    private BigDecimal targetCarbs;
    private BigDecimal targetFat;
    
    // 完成百分比
    private Integer caloriesPercent;
    private Integer proteinPercent;
    private Integer carbsPercent;
    private Integer fatPercent;
    
    // 餐别分布
    private Map<String, MealNutrition> meals;
    
    // 建议
    private List<String> suggestions;
    
    /**
     * 单餐营养
     */
    @Data
    public static class MealNutrition {
        private String mealType;        // 餐别名称
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
        private Integer percent;        // 占全天比例
    }
}