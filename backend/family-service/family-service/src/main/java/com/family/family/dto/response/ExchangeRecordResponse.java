package com.family.family.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 兑换记录响应DTO
 */
@Data
public class ExchangeRecordResponse {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 券ID
     */
    private Long couponId;
    
    /**
     * 券名称
     */
    private String couponName;
    
    /**
     * 消耗积分
     */
    private Integer pointsSpent;
    
    /**
     * 兑换数量
     */
    private Integer quantity;
    
    /**
     * 状态: pending-待发放, delivered-已发放, used-已使用, expired-已过期
     */
    private String status;
    
    /**
     * 过期日期
     */
    private LocalDate expireDate;
    
    /**
     * 兑换码
     */
    private String exchangeCode;
    
    /**
     * 兑换时间
     */
    private LocalDateTime exchangeTime;
    
    /**
     * 使用时间
     */
    private LocalDateTime useTime;
}
