package com.family.common.core;

/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    SUCCESS(200, "success"),
    ERROR(500, "系统错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    USER_NOT_LOGIN(1004, "用户未登录"),
    
    FAMILY_NOT_FOUND(2001, "家庭不存在"),
    FAMILY_ALREADY_EXISTS(2002, "家庭已存在"),
    FAMILY_MEMBER_ALREADY_EXISTS(2003, "已经是家庭成员"),
    FAMILY_PERMISSION_DENIED(2004, "没有家庭权限"),
    
    TASK_NOT_FOUND(3001, "任务不存在"),
    WISH_NOT_FOUND(4001, "心愿不存在"),
    RECIPE_NOT_FOUND(5001, "菜谱不存在"),
    FILE_UPLOAD_ERROR(6001, "文件上传失败");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
