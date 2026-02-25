package com.family.vote.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;

import java.time.LocalDateTime;

/**
 * 投票实体
 */
@TableName("vote")
public class Vote extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private Long creatorId;
    private String title;
    private String description;
    private String voteType;
    private Integer maxSelect;
    private Boolean isAnonymous;
    private Boolean canChange;
    private Integer minVotes;
    private Double passThreshold;
    private LocalDateTime endTime;
    private Integer status;
    private String options;
    private String result;
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVoteType() {
        return voteType;
    }
    
    public void setVoteType(String voteType) {
        this.voteType = voteType;
    }
    
    public Integer getMaxSelect() {
        return maxSelect;
    }
    
    public void setMaxSelect(Integer maxSelect) {
        this.maxSelect = maxSelect;
    }
    
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }
    
    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }
    
    public Boolean getCanChange() {
        return canChange;
    }
    
    public void setCanChange(Boolean canChange) {
        this.canChange = canChange;
    }
    
    public Integer getMinVotes() {
        return minVotes;
    }
    
    public void setMinVotes(Integer minVotes) {
        this.minVotes = minVotes;
    }
    
    public Double getPassThreshold() {
        return passThreshold;
    }
    
    public void setPassThreshold(Double passThreshold) {
        this.passThreshold = passThreshold;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getOptions() {
        return options;
    }
    
    public void setOptions(String options) {
        this.options = options;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
}
