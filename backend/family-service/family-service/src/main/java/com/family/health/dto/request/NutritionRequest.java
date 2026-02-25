package com.family.health.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 营养分析请求DTO
 */
@Data
public class NutritionRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分析日期
     */
    private LocalDate date;
    
    /**
     * 开始日期(用于区间分析)
     */
    private LocalDate startDate;
    
    /**
     * 结束日期(用于区间分析)
     */
    private LocalDate endDate;
    
    /**
     * 分析类型: daily-每日, weekly-每周, monthly-每月
     */
    private String analysisType;
    
    /**
     * 目标卡路里
     */
    private Integer targetCalories;
    
    /**
     * 目标蛋白质(克)
     */
    private Integer targetProtein;
    
    /**
     * 目标碳水(克)
     */
    private Integer targetCarbs;
    
    /**
     * 目标脂肪(克)
     */
    private Integer targetFat;
}
