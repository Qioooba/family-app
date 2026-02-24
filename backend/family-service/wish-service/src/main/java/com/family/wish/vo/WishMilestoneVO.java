package com.family.wish.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心愿里程碑VO
 */
public class WishMilestoneVO {
    
    private Long id;
    private Long wishId;
    private String wishTitle;
    private String title;
    private String description;
    private LocalDate targetDate;
    private Integer isCompleted;
    private Integer sortOrder;
    private LocalDateTime createTime;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getWishId() { return wishId; }
    public void setWishId(Long wishId) { this.wishId = wishId; }
    
    public String getWishTitle() { return wishTitle; }
    public void setWishTitle(String wishTitle) { this.wishTitle = wishTitle; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    
    public Integer getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Integer isCompleted) { this.isCompleted = isCompleted; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
