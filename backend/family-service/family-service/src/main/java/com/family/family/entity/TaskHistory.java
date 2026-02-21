package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务历史/操作日志实体
 */
@Data
@TableName("task_history")
public class TaskHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    private Long userId;
    private String action; // create/update/delete/complete/assign等
    private String fieldName; // 修改的字段名
    private String oldValue;
    private String newValue;
    private String remark;
    private LocalDateTime createTime;
}
