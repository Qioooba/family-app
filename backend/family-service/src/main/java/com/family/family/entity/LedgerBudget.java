package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账本预算
 */
@TableName("ledger_budget")
public class LedgerBudget implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long ledgerId;
    private Long familyId;
    private Long categoryId;
    private String budgetMonth;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private BigDecimal alertThreshold;
    private Integer alertEnabled;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getLedgerId() {
        return ledgerId;
    }
    
    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
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
    
    public String getBudgetMonth() {
        return budgetMonth;
    }
    
    public void setBudgetMonth(String budgetMonth) {
        this.budgetMonth = budgetMonth;
    }
    
    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }
    
    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
    
    public BigDecimal getUsedAmount() {
        return usedAmount;
    }
    
    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }
    
    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }
    
    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold;
    }
    
    public Integer getAlertEnabled() {
        return alertEnabled;
    }
    
    public void setAlertEnabled(Integer alertEnabled) {
        this.alertEnabled = alertEnabled;
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
