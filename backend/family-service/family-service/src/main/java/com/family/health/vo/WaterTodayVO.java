package com.family.health.vo;

import com.family.family.entity.WaterRecord;

import java.util.List;

/**
 * 今日喝水统计VO
 */
public class WaterTodayVO {
    
    private Long userId;
    private String date;
    private Integer todayAmount;
    private Integer targetAmount;
    private Integer percent;
    private String status;
    private String message;
    private List<WaterRecord> records;
    
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
    
    public Integer getTodayAmount() {
        return todayAmount;
    }
    
    public void setTodayAmount(Integer todayAmount) {
        this.todayAmount = todayAmount;
    }
    
    public Integer getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(Integer targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public Integer getPercent() {
        return percent;
    }
    
    public void setPercent(Integer percent) {
        this.percent = percent;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<WaterRecord> getRecords() {
        return records;
    }
    
    public void setRecords(List<WaterRecord> records) {
        this.records = records;
    }
}
