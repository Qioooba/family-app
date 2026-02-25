package com.family.family.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务提醒VO
 */
public class TaskReminderVO {
    
    private Long id;
    private Long taskId;
    private String taskTitle; // 任务标题
    private String reminderType; // time-时间提醒 location-位置提醒
    private LocalDateTime reminderTime; // 提醒时间
    private String locationName; // 位置名称
    private BigDecimal locationLat; // 纬度
    private BigDecimal locationLng; // 经度
    private Integer radius; // 地理围栏半径
    private Integer isTriggered; // 是否已触发
    private LocalDateTime createTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskTitle() {
        return taskTitle;
    }
    
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
    
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
    
    public Integer getIsTriggered() {
        return isTriggered;
    }
    
    public void setIsTriggered(Integer isTriggered) {
        this.isTriggered = isTriggered;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}