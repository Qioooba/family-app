package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒执行日志
 */
@Data
@TableName("reminder_log")
public class ReminderLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reminderId;
    private Long userId;
    
    private LocalDateTime executeTime;
    private LocalDateTime actualExecuteTime;
    private LocalDateTime finishTime;
    
    private String title;
    private String content;
    
    private String pushStatus;
    private String pushResult;
    private String errorMsg;
    
    private Integer readStatus;
    private LocalDateTime readTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
