package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 食材实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ingredient")
public class Ingredient extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
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
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public String getStorageLocation() {
        return storageLocation;
    }
    
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDate getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }
    
    public Integer getReminderDays() {
        return reminderDays;
    }
    
    public void setReminderDays(Integer reminderDays) {
        this.reminderDays = reminderDays;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getRecognizedData() {
        return recognizedData;
    }
    
    public void setRecognizedData(String recognizedData) {
        this.recognizedData = recognizedData;
    }
}
