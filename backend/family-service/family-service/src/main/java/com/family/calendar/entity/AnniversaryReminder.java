package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 纪念日提醒设置实体
 */
@TableName("anniversary_reminder")
public class AnniversaryReminder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long anniversaryId;
    private Integer reminderDays;
    private String reminderType;
    private Integer isEnabled;
    private LocalDateTime createTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAnniversaryId() {
        return anniversaryId;
    }
    
    public void setAnniversaryId(Long anniversaryId) {
        this.anniversaryId = anniversaryId;
    }
    
    public Integer getReminderDays() {
        return reminderDays;
    }
    
    public void setReminderDays(Integer reminderDays) {
        this.reminderDays = reminderDays;
    }
    
    public String getReminderType() {
        return reminderType;
    }
    
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
    
    public Integer getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
