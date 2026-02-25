package com.family.family.dto.request;

import lombok.Data;

/**
 * 兑换请求DTO
 */
@Data
public class ExchangeRequest {
    
    /**
     * 兑换券ID
     */
    private Long couponId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 兑换数量
     */
    private Integer quantity;
    
    /**
     * 备注
     */
    private String remark;
}
