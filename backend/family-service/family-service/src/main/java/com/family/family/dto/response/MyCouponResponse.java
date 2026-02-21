package com.family.family.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的优惠券响应DTO
 */
@Data
public class MyCouponResponse {
    
    /**
     * 记录ID
     */
    private Long recordId;
    
    /**
     * 券ID
     */
    private Long couponId;
    
    /**
     * 券名称
     */
    private String couponName;
    
    /**
     * 券描述
     */
    private String description;
    
    /**
     * 券类型
     */
    private String couponType;
    
    /**
     * 券图标
     */
    private String icon;
    
    /**
     * 消耗积分
     */
    private Integer pointsSpent;
    
    /**
     * 兑换码
     */
    private String exchangeCode;
    
    /**
     * 二维码图片URL
     */
    private String qrCodeUrl;
    
    /**
     * 状态: unused-未使用, used-已使用, expired-已过期
     */
    private String status;
    
    /**
     * 获得时间
     */
    private LocalDateTime obtainTime;
    
    /**
     * 过期日期
     */
    private LocalDate expireDate;
    
    /**
     * 使用时间
     */
    private LocalDateTime useTime;
    
    /**
     * 剩余天数
     */
    private Integer remainingDays;
    
    /**
     * 是否即将过期(7天内)
     */
    private Boolean isExpiringSoon;
}
