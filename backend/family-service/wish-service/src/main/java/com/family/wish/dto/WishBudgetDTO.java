package com.family.wish.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 心愿预算DTO
 */
@Data
public class WishBudgetDTO {
    
    private BigDecimal budgetMin; // 预算下限
    private BigDecimal budgetMax; // 预算上限
    private String currency;      // 货币类型，默认CNY
}