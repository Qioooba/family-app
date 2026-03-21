package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 任务实体
 */
@TableName("task")
public class Task {
    
    /**
     * 支持多种日期格式的 LocalDateTime 反序列化器
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                    // 2026-03-08T15:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),       // 2026-03-08 15:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),          // 2026-03-08 15:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd")                 // 2026-03-08
        };
        
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isEmpty()) {
                return null;
            }
            
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(text, formatter);
                } catch (DateTimeParseException ignored) {
                    // 尝试下一个格式
                }
            }
            
            throw new IOException("无法解析日期时间: " + text);
        }
    }

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private Long categoryId;
    private String title;
    private String content;
    private String remark;
    private Integer priority;
    private Integer status;
    private Long assigneeId;
    private Long creatorId;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime dueTime;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime remindTime;
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
    private Integer sortOrder;

    // 重复规则字段
    private String repeatType;         // 重复类型: none/daily/weekly/monthly/yearly/workday/custom
    private String repeatRule;         // JSON格式的重复规则配置
    private java.time.LocalDate repeatStartDate;  // 重复开始日期
    private java.time.LocalDate repeatEndDate;    // 重复结束日期
    private LocalDateTime nextRunTime; // 下次执行时间

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
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public java.time.LocalDate getRepeatStartDate() {
        return repeatStartDate;
    }

    public void setRepeatStartDate(java.time.LocalDate repeatStartDate) {
        this.repeatStartDate = repeatStartDate;
    }

    public java.time.LocalDate getRepeatEndDate() {
        return repeatEndDate;
    }

    public void setRepeatEndDate(java.time.LocalDate repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }

    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }
}
