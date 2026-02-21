package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 任务标签关联实体
 */
@Data
@TableName("task_tag_relation")
public class TaskTagRelation {
    
    private Long taskId;
    private Long tagId;
}
