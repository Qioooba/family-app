package com.family.health.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 营养周报响应DTO
 */
@Data
public class NutritionWeeklyResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 周开始日期
     */
    private String weekStart;
    
    /**
     * 周结束日期
     */
    private String weekEnd;
    
    /**
     * 平均每日卡路里
     */
    private BigDecimal avgDailyCalories;
    
    /**
     * 总蛋白质(克)
     */
    private BigDecimal totalProtein;
    
    /**
     * 总碳水(克)
     */
    private BigDecimal totalCarbs;
    
    /**
     * 总脂肪(克)
     */
    private BigDecimal totalFat;
    
    /**
     * 每日数据
     */
    private List<DailyNutrition> dailyData;
    
    /**
     * 营养趋势
     */
    private Map<String, String> trends;
    
    /**
     * 周报总结
     */
    private String summary;
    
    /**
     * 改进建议
     */
    private List<String> recommendations;
    
    /**
     * 每日营养
     */
    @Data
    public static class DailyNutrition {
        private String date;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
        private Boolean reachedTarget;
    }
}
