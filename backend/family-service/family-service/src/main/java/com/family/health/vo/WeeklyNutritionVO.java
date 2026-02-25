package com.family.health.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 周营养报告VO
 */
public class WeeklyNutritionVO {
    
    private Long userId;
    private String weekStart;
    private String weekEnd;
    private BigDecimal avgCalories;
    private BigDecimal avgProtein;
    private BigDecimal avgCarbs;
    private BigDecimal avgFat;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    private Integer caloriesAchievement;
    private Integer proteinAchievement;
    private List<DailySummary> dailySummaries;
    private Map<String, List<BigDecimal>> trends;
    private Integer healthScore;
    private List<String> recommendations;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getWeekStart() {
        return weekStart;
    }
    
    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }
    
    public String getWeekEnd() {
        return weekEnd;
    }
    
    public void setWeekEnd(String weekEnd) {
        this.weekEnd = weekEnd;
    }
    
    public BigDecimal getAvgCalories() {
        return avgCalories;
    }
    
    public void setAvgCalories(BigDecimal avgCalories) {
        this.avgCalories = avgCalories;
    }
    
    public BigDecimal getAvgProtein() {
        return avgProtein;
    }
    
    public void setAvgProtein(BigDecimal avgProtein) {
        this.avgProtein = avgProtein;
    }
    
    public BigDecimal getAvgCarbs() {
        return avgCarbs;
    }
    
    public void setAvgCarbs(BigDecimal avgCarbs) {
        this.avgCarbs = avgCarbs;
    }
    
    public BigDecimal getAvgFat() {
        return avgFat;
    }
    
    public void setAvgFat(BigDecimal avgFat) {
        this.avgFat = avgFat;
    }
    
    public BigDecimal getTotalCalories() {
        return totalCalories;
    }
    
    public void setTotalCalories(BigDecimal totalCalories) {
        this.totalCalories = totalCalories;
    }
    
    public BigDecimal getTotalProtein() {
        return totalProtein;
    }
    
    public void setTotalProtein(BigDecimal totalProtein) {
        this.totalProtein = totalProtein;
    }
    
    public BigDecimal getTotalCarbs() {
        return totalCarbs;
    }
    
    public void setTotalCarbs(BigDecimal totalCarbs) {
        this.totalCarbs = totalCarbs;
    }
    
    public BigDecimal getTotalFat() {
        return totalFat;
    }
    
    public void setTotalFat(BigDecimal totalFat) {
        this.totalFat = totalFat;
    }
    
    public Integer getCaloriesAchievement() {
        return caloriesAchievement;
    }
    
    public void setCaloriesAchievement(Integer caloriesAchievement) {
        this.caloriesAchievement = caloriesAchievement;
    }
    
    public Integer getProteinAchievement() {
        return proteinAchievement;
    }
    
    public void setProteinAchievement(Integer proteinAchievement) {
        this.proteinAchievement = proteinAchievement;
    }
    
    public List<DailySummary> getDailySummaries() {
        return dailySummaries;
    }
    
    public void setDailySummaries(List<DailySummary> dailySummaries) {
        this.dailySummaries = dailySummaries;
    }
    
    public Map<String, List<BigDecimal>> getTrends() {
        return trends;
    }
    
    public void setTrends(Map<String, List<BigDecimal>> trends) {
        this.trends = trends;
    }
    
    public Integer getHealthScore() {
        return healthScore;
    }
    
    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    /**
     * 每日摘要
     */
    public static class DailySummary {
        private String date;
        private BigDecimal calories;
        private BigDecimal protein;
        private Boolean goalAchieved;
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public BigDecimal getCalories() {
            return calories;
        }
        
        public void setCalories(BigDecimal calories) {
            this.calories = calories;
        }
        
        public BigDecimal getProtein() {
            return protein;
        }
        
        public void setProtein(BigDecimal protein) {
            this.protein = protein;
        }
        
        public Boolean getGoalAchieved() {
            return goalAchieved;
        }
        
        public void setGoalAchieved(Boolean goalAchieved) {
            this.goalAchieved = goalAchieved;
        }
    }
}
