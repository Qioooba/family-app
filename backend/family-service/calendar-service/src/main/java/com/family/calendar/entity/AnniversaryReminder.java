package com.family.calendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 纪念日提醒设置实体
 */
@Data
@TableName("anniversary_reminder")
public class AnniversaryReminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long anniversaryId;
    private Integer reminderDays;   // 提前几天提醒
    private String reminderType;    // app/sms/email
    private Integer isEnabled;      // 是否启用
    private LocalDateTime createTime;
}