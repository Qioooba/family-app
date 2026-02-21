package com.family.health.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 周营养报告VO
 */
@Data
public class WeeklyNutritionVO {
    
    private Long userId;
    private String weekStart;
    private String weekEnd;
    
    // 周平均摄入
    private BigDecimal avgCalories;
    private BigDecimal avgProtein;
    private BigDecimal avgCarbs;
    private BigDecimal avgFat;
    
    // 周总摄入
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    
    // 目标完成率
    private Integer caloriesAchievement;
    private Integer proteinAchievement;
    
    // 每日数据
    private List<DailySummary> dailySummaries;
    
    // 营养趋势
    private Map<String, List<BigDecimal>> trends;
    
    // 健康评分
    private Integer healthScore;
    private List<String> recommendations;
    
    /**
     * 每日摘要
     */
    @Data
    public static class DailySummary {
        private String date;
        private BigDecimal calories;
        private BigDecimal protein;
        private Boolean goalAchieved;
    }
}