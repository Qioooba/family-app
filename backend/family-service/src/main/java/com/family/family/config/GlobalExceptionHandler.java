package com.family.family.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.family.common.core.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理Sa-Token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        // 未登录是前端状态流转中的常见结果，不记录为告警，避免日志被噪音淹没。
        log.info("用户未登录: {}", e.getMessage());
        return Result.error(401, "请先登录");
    }

    /**
     * 处理Sa-Token未授权异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "无权操作");
    }

    /**
     * 处理Sa-Token角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色权限不足: {}", e.getMessage());
        return Result.error(403, "角色权限不足");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "无权操作");
    }
}
