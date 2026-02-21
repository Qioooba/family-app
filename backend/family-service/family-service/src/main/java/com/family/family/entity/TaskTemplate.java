package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务模板实体
 */
@Data
@TableName("task_template")
public class TaskTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private Long creatorId;
    private String name;
    private String title; // 任务标题模板
    private String content; // 任务内容模板
    private Long categoryId;
    private Integer priority;
    private String repeatType;
    private String repeatRule;
    private String tags; // JSON格式标签
    private Integer estimatedMinutes; // 预计耗时
    private Integer isPublic; // 是否公开
    private Integer usageCount; // 使用次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
