package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家庭报告实体
 */
@TableName("family_report")
public class FamilyReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private String reportType;      // weekly/monthly/yearly
    private LocalDate reportDate;
    private String title;
    private String content;         // JSON格式
    private Integer score;
    private Integer isRead;
    private LocalDateTime createTime;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}