package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 任务标签关联实体
 */
@TableName("task_tag_relation")
public class TaskTagRelation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long taskId;
    private Long tagId;
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
