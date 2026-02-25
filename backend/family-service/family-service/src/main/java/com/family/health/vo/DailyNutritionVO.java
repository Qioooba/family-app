package com.family.health.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 每日营养分析VO
 */
public class DailyNutritionVO {
    
    private Long userId;
    private String date;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    private BigDecimal targetCalories;
    private BigDecimal targetProtein;
    private BigDecimal targetCarbs;
    private BigDecimal targetFat;
    private Integer caloriesPercent;
    private Integer proteinPercent;
    private Integer carbsPercent;
    private Integer fatPercent;
    private Map<String, MealNutrition> meals;
    private List<String> suggestions;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
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
    
    public BigDecimal getTotalFiber() {
        return totalFiber;
    }
    
    public void setTotalFiber(BigDecimal totalFiber) {
        this.totalFiber = totalFiber;
    }
    
    public BigDecimal getTargetCalories() {
        return targetCalories;
    }
    
    public void setTargetCalories(BigDecimal targetCalories) {
        this.targetCalories = targetCalories;
    }
    
    public BigDecimal getTargetProtein() {
        return targetProtein;
    }
    
    public void setTargetProtein(BigDecimal targetProtein) {
        this.targetProtein = targetProtein;
    }
    
    public BigDecimal getTargetCarbs() {
        return targetCarbs;
    }
    
    public void setTargetCarbs(BigDecimal targetCarbs) {
        this.targetCarbs = targetCarbs;
    }
    
    public BigDecimal getTargetFat() {
        return targetFat;
    }
    
    public void setTargetFat(BigDecimal targetFat) {
        this.targetFat = targetFat;
    }
    
    public Integer getCaloriesPercent() {
        return caloriesPercent;
    }
    
    public void setCaloriesPercent(Integer caloriesPercent) {
        this.caloriesPercent = caloriesPercent;
    }
    
    public Integer getProteinPercent() {
        return proteinPercent;
    }
    
    public void setProteinPercent(Integer proteinPercent) {
        this.proteinPercent = proteinPercent;
    }
    
    public Integer getCarbsPercent() {
        return carbsPercent;
    }
    
    public void setCarbsPercent(Integer carbsPercent) {
        this.carbsPercent = carbsPercent;
    }
    
    public Integer getFatPercent() {
        return fatPercent;
    }
    
    public void setFatPercent(Integer fatPercent) {
        this.fatPercent = fatPercent;
    }
    
    public Map<String, MealNutrition> getMeals() {
        return meals;
    }
    
    public void setMeals(Map<String, MealNutrition> meals) {
        this.meals = meals;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    /**
     * 单餐营养
     */
    public static class MealNutrition {
        private String mealType;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
        private Integer percent;
        
        public String getMealType() {
            return mealType;
        }
        
        public void setMealType(String mealType) {
            this.mealType = mealType;
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
        
        public BigDecimal getCarbs() {
            return carbs;
        }
        
        public void setCarbs(BigDecimal carbs) {
            this.carbs = carbs;
        }
        
        public BigDecimal getFat() {
            return fat;
        }
        
        public void setFat(BigDecimal fat) {
            this.fat = fat;
        }
        
        public Integer getPercent() {
            return percent;
        }
        
        public void setPercent(Integer percent) {
            this.percent = percent;
        }
    }
}
