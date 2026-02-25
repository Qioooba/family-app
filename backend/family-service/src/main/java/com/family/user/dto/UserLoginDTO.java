package com.family.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {
    
    /**
     * 用户名（用户名密码登录时必填）
     */
    private String username;
    
    /**
     * 密码（用户名密码登录时必填）
     */
    private String password;
    
    /**
     * 手机号（验证码登录时必填）
     */
    private String phone;
    
    /**
     * 验证码（验证码登录时必填）
     */
    private String code;
    
    /**
     * 登录类型：password-用户名密码登录，sms-验证码登录
     */
    @NotNull(message = "登录类型不能为空")
    private String loginType;
}
