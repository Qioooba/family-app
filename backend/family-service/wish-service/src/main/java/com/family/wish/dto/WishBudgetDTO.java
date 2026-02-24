package com.family.wish.dto;

import java.math.BigDecimal;

/**
 * 心愿预算DTO
 */
public class WishBudgetDTO {
    
    private BigDecimal budgetMin; // 预算下限
    private BigDecimal budgetMax; // 预算上限
    private String currency;      // 货币类型，默认CNY
    
    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
    
    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
