package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务评论实体
 */
@Data
@TableName("task_comment")
public class TaskComment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    private Long userId;
    private String content;
    private Long parentId; // 回复的评论ID
    private Integer replyCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
