package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场景提醒日志实体
 */
@Data
@TableName("scene_reminder_log")
public class SceneReminderLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reminderId;

    private Long userId;

    private String sceneType;

    private LocalDate remindDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
