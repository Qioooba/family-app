package com.family.family.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务提醒DTO
 */
public class TaskReminderDTO {
    
    private String reminderType; // time-时间提醒 location-位置提醒
    private LocalDateTime reminderTime; // 提醒时间（时间提醒用）
    private String locationName; // 位置名称（位置提醒用）
    private BigDecimal locationLat; // 纬度（位置提醒用）
    private BigDecimal locationLng; // 经度（位置提醒用）
    private Integer radius; // 地理围栏半径(米)，默认500米
    
    public String getReminderType() {
        return reminderType;
    }
    
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
    
    public LocalDateTime getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public BigDecimal getLocationLat() {
        return locationLat;
    }
    
    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }
    
    public BigDecimal getLocationLng() {
        return locationLng;
    }
    
    public void setLocationLng(BigDecimal locationLng) {
        this.locationLng = locationLng;
    }
    
    public Integer getRadius() {
        return radius;
    }
    
    public void setRadius(Integer radius) {
        this.radius = radius;
    }
}