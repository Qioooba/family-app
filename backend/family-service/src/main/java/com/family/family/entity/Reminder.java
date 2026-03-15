package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒配置实体（完整版）
 */
@Data
@TableName("reminder_config")
public class Reminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 基础信息
    private String reminderName;
    private String reminderType;
    private Integer createType;
    private Long createBy;
    
    // 推送范围
    private String pushScope;
    private String targetUserIds;  // JSON格式
    
    // 时间配置
    private String cronExpression;
    private String remindTime;  // 提醒时间，如 08:00，前端直接使用
    private String frequencyType;  // ONCE/DAILY/HOURLY/WEEKLY/MONTHLY/YEARLY/INTERVAL
    private String frequencyConfig; // JSON格式
    
    // 内容配置
    private String titleTemplate;
    private String contentTemplate;
    private String businessData;   // JSON格式
    
    // 执行配置
    private LocalDateTime nextExecuteTime;
    private LocalDateTime lastExecuteTime;
    private String lastExecuteResult;
    private Integer executeCount;
    private Integer maxExecuteCount;
    
    // 状态控制
    private Integer status;
    private Integer priority;
    
    // 免打扰
    private String quietHoursStart;
    private String quietHoursEnd;
    
    // 元数据
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
