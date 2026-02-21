package com.family.family.dto.request;

import lombok.Data;

/**
 * 我的优惠券使用请求DTO
 */
@Data
public class MyCouponUseRequest {
    
    /**
     * 记录ID
     */
    private Long recordId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 使用备注
     */
    private String remark;
}
