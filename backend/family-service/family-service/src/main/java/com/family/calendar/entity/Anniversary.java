package com.family.calendar.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念日实体
 */
@TableName("calendar_anniversary")
public class Anniversary extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private String title;
    private String type;
    private String dateType;
    private LocalDate targetDate;
    private Boolean isRecurring;
    private String reminderDays;
    private Long relatedUserId;
    private String description;
    private String images;
    private String icon;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDateType() {
        return dateType;
    }
    
    public void setDateType(String dateType) {
        this.dateType = dateType;
    }
    
    public LocalDate getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public String getReminderDays() {
        return reminderDays;
    }
    
    public void setReminderDays(String reminderDays) {
        this.reminderDays = reminderDays;
    }
    
    public Long getRelatedUserId() {
        return relatedUserId;
    }
    
    public void setRelatedUserId(Long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
