package com.family.health.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.family.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 健康服务全局异常处理器
 * 确保 Sa-Token 异常被正确处理，避免返回 401 触发前端登出
 */
@Slf4j
@RestControllerAdvice
public class HealthGlobalExceptionHandler {

    /**
     * Sa-Token未登录异常
     * 返回 4010 而不是 401，避免前端全局登出逻辑
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        return Result.error(4010, "请先登录");
    }

    /**
     * Sa-Token异常
     */
    @ExceptionHandler(SaTokenException.class)
    public Result<Void> handleSaTokenException(SaTokenException e) {
        log.warn("Sa-Token异常: {}", e.getMessage());
        return Result.error(4010, e.getMessage());
    }

    /**
     * 其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
