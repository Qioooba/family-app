package com.family.wish.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算统计VO
 */
@Data
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
    private java.util.List<BudgetByTypeVO> budgetByType;
    
    /**
     * 按类型预算统计
     */
    @Data
    public static class BudgetByTypeVO {
        private String type;             // 类型
        private String typeName;         // 类型名称
        private Integer count;           // 数量
        private BigDecimal budgetMin;    // 预算下限合计
        private BigDecimal budgetMax;    // 预算上限合计
    }
}