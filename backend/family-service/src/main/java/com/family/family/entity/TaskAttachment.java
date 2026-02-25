package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务附件实体
 */
@Data
@TableName("task_attachment")
public class TaskAttachment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    private Long uploaderId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createTime;
}
