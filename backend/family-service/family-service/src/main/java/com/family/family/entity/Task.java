package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务实体
 */
@TableName("task")
public class Task {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isArchived;
    private LocalDateTime archiveTime;
    private Integer isDeleted;
    private LocalDateTime deleteTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Long getAssigneeId() {
        return assigneeId;
    }
    
    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public LocalDateTime getDueTime() {
        return dueTime;
    }
    
    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }
    
    public LocalDateTime getRemindTime() {
        return remindTime;
    }
    
    public void setRemindTime(LocalDateTime remindTime) {
        this.remindTime = remindTime;
    }
    
    public String getRepeatType() {
        return repeatType;
    }
    
    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }
    
    public String getRepeatRule() {
        return repeatRule;
    }
    
    public void setRepeatRule(String repeatRule) {
        this.repeatRule = repeatRule;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getAttachments() {
        return attachments;
    }
    
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }
    
    public LocalDateTime getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getIsArchived() {
        return isArchived;
    }
    
    public void setIsArchived(Integer isArchived) {
        this.isArchived = isArchived;
    }
    
    public LocalDateTime getArchiveTime() {
        return archiveTime;
    }
    
    public void setArchiveTime(LocalDateTime archiveTime) {
        this.archiveTime = archiveTime;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }
    
    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }
}
