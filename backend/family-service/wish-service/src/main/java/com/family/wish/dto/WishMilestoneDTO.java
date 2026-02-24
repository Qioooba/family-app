package com.family.wish.dto;

import java.time.LocalDate;

/**
 * 心愿里程碑DTO
 */
public class WishMilestoneDTO {
    
    private String title;
    private String description;
    private LocalDate targetDate;
    private Integer sortOrder;
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
