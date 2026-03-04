package com.family.family.dto;

import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WxLoginRequest {
    /**
     * 微信登录凭证
     */
    private String wxCode;
    
    /**
     * 加密的手机号数据
     */
    private String encryptedData;
    
    /**
     * 加密算法的初始向量
     */
    private String iv;
    
    /**
     * 登录类型
     */
    private String loginType;
    
    /**
     * 手机号（用于绑定）
     */
    private String phone;
    
    /**
     * openid（用于绑定）
     */
    private String openid;
    
    /**
     * 邀请码（用于绑定验证）
     */
    private String code;
}
