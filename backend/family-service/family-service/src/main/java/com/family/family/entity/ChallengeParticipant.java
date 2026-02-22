package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挑战参与者
 */
@TableName("challenge_participant")
public class ChallengeParticipant implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long challengeId;
    private Long familyId;
    private Long userId;
    private BigDecimal currentValue;
    private Integer progress;
    private Integer checkinCount;
    private LocalDate lastCheckinDate;
    private Integer isCompleted;
    private LocalDate completeTime;
    private Integer rank;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }
    
    public Long getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public Integer getCheckinCount() {
        return checkinCount;
    }
    
    public void setCheckinCount(Integer checkinCount) {
        this.checkinCount = checkinCount;
    }
    
    public LocalDate getLastCheckinDate() {
        return lastCheckinDate;
    }
    
    public void setLastCheckinDate(LocalDate lastCheckinDate) {
        this.lastCheckinDate = lastCheckinDate;
    }
    
    public Integer getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Integer isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public LocalDate getCompleteTime() {
        return completeTime;
    }
    
    public void setCompleteTime(LocalDate completeTime) {
        this.completeTime = completeTime;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
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
}
