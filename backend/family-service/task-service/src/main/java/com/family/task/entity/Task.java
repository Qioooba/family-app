package com.family.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    
    @TableField(exist = false)
    private Long categoryId;
    private String title;
    private String content;
    private Integer priority;
    private Integer status;
    private Long assigneeId;
    private Long creatorId;
    private LocalDateTime dueTime;
    private LocalDateTime remindTime;
    private String repeatType;
    private String repeatRule;
    private String location;
    private Long parentId;
    private String attachments;
    private LocalDateTime finishTime;
}
