package com.family.wish.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算统计VO
 */
public class BudgetStatsVO {
    
    // 总体统计
    private Integer totalWishCount;      // 心愿总数
    private Integer pendingCount;        // 待实现数量
    private Integer inProgressCount;     // 进行中数量
    private Integer completedCount;      // 已完成数量
    
    // 预算统计
    private BigDecimal totalBudgetMin;   // 总预算下限
    private BigDecimal totalBudgetMax;   // 总预算上限
    private BigDecimal completedCost;    // 已完成心愿实际花费
    private BigDecimal estimatedCost;    // 预计总花费（进行中按预算平均值）
    
    // 分类统计
    private List<BudgetByTypeVO> budgetByType;
    
    // 手动添加getter/setter
    public Integer getTotalWishCount() { return totalWishCount; }
    public void setTotalWishCount(Integer totalWishCount) { this.totalWishCount = totalWishCount; }
    
    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    
    public Integer getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(Integer inProgressCount) { this.inProgressCount = inProgressCount; }
    
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    
    public BigDecimal getTotalBudgetMin() { return totalBudgetMin; }
    public void setTotalBudgetMin(BigDecimal totalBudgetMin) { this.totalBudgetMin = totalBudgetMin; }
    
    public BigDecimal getTotalBudgetMax() { return totalBudgetMax; }
    public void setTotalBudgetMax(BigDecimal totalBudgetMax) { this.totalBudgetMax = totalBudgetMax; }
    
    public BigDecimal getCompletedCost() { return completedCost; }
    public void setCompletedCost(BigDecimal completedCost) { this.completedCost = completedCost; }
    
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    
    public List<BudgetByTypeVO> getBudgetByType() { return budgetByType; }
    public void setBudgetByType(List<BudgetByTypeVO> budgetByType) { this.budgetByType = budgetByType; }
    
    /**
     * 按类型预算统计
     */
    public static class BudgetByTypeVO {
        private String type;             // 类型
        private String typeName;         // 类型名称
        private Integer count;           // 数量
        private BigDecimal budgetMin;    // 预算下限合计
        private BigDecimal budgetMax;    // 预算上限合计
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        
        public BigDecimal getBudgetMin() { return budgetMin; }
        public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
        
        public BigDecimal getBudgetMax() { return budgetMax; }
        public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
    }
}
