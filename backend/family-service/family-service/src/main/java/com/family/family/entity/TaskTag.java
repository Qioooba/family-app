package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务标签实体
 */
@Data
@TableName("task_tag")
public class TaskTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private String name;
    private String color;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
