package com.family.family.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 兑换券响应DTO
 */
@Data
public class ExchangeCouponResponse {
    
    /**
     * 券ID
     */
    private Long id;
    
    /**
     * 券名称
     */
    private String name;
    
    /**
     * 券描述
     */
    private String description;
    
    /**
     * 券类型: reward-奖励券, privilege-特权券, gift-礼品券
     */
    private String couponType;
    
    /**
     * 所需积分
     */
    private Integer pointsCost;
    
    /**
     * 市场价值
     */
    private BigDecimal marketValue;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 每人限购数量
     */
    private Integer limitPerUser;
    
    /**
     * 有效期(天)
     */
    private Integer validDays;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 券图标
     */
    private String icon;
    
    /**
     * 是否热门
     */
    private Boolean isHot;
    
    /**
     * 剩余库存
     */
    private Integer remainingStock;
}
