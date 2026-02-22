package com.family.health.entity;

import com.family.common.core.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食记录实体
 */
public class DietRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long familyId;
    private String mealType;
    private LocalDate recordDate;
    private LocalDateTime recordTime;
    private String foodName;
    private BigDecimal quantity;
    private String unit;
    private Integer calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private BigDecimal fiber;
    private String image;
    private String recognitionSource;
    private String aiAnalysis;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public LocalDateTime getRecordTime() {
        return recordTime;
    }
    
    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Integer getCalories() {
        return calories;
    }
    
    public void setCalories(Integer calories) {
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
    
    public BigDecimal getFiber() {
        return fiber;
    }
    
    public void setFiber(BigDecimal fiber) {
        this.fiber = fiber;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getRecognitionSource() {
        return recognitionSource;
    }
    
    public void setRecognitionSource(String recognitionSource) {
        this.recognitionSource = recognitionSource;
    }
    
    public String getAiAnalysis() {
        return aiAnalysis;
    }
    
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}
