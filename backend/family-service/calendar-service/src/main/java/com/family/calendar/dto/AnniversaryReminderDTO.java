package com.family.calendar.dto;

import lombok.Data;

/**
 * 纪念日提醒DTO
 */
@Data
public class AnniversaryReminderDTO {
    
    private Long anniversaryId;
    private Integer reminderDays;   // 提前几天提醒
    private String reminderType;    // app/sms/email
    private Integer isEnabled;
}