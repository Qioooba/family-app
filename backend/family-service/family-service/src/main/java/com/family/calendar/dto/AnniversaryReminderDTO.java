package com.family.calendar.dto;

/**
 * 纪念日提醒DTO
 */
public class AnniversaryReminderDTO {
    
    private Long anniversaryId;
    private Integer reminderDays;
    private String reminderType;
    private Integer isEnabled;
    
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
}
