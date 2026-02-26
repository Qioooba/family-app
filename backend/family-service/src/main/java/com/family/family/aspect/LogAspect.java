package com.family.family.aspect;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.OperationLog;
import com.family.family.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 定义切入点 - 所有Controller方法
     */
    @Pointcut("execution(* com.family.family.controller.*.*(..))")
    public void controllerPointcut() {}
    
    /**
     * 环绕通知记录操作日志
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        // 执行目标方法
        Object result = null;
        Exception exception = null;
        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 保存日志
            saveLog(point, request, result, exception, System.currentTimeMillis() - startTime);
        }
    }
    
    private void saveLog(ProceedingJoinPoint point, HttpServletRequest request, 
                        Object result, Exception exception, long executionTime) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setIp(getClientIp(request));
            operationLog.setExecutionTime(executionTime);
            operationLog.setCreateTime(LocalDateTime.now());
            
            // 请求参数
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                try {
                    operationLog.setRequestParams(JSONUtil.toJsonStr(args[0]));
                } catch (Exception e) {
                    operationLog.setRequestParams(args[0].toString());
                }
            }
            
            // 响应结果
            if (result != null) {
                try {
                    operationLog.setResponseData(JSONUtil.toJsonStr(result).substring(0, Math.min(1000, JSONUtil.toJsonStr(result).length())));
                } catch (Exception e) {
                    operationLog.setResponseData(result.toString());
                }
            }
            
            // 操作状态
            if (exception == null) {
                operationLog.setStatus(1);
            } else {
                operationLog.setStatus(0);
                operationLog.setErrorMsg(exception.getMessage());
            }
            
            // 模块和操作
            String className = point.getTarget().getClass().getSimpleName();
            String methodName = point.getSignature().getName();
            operationLog.setModule(getModuleName(className));
            operationLog.setOperation(methodName);
            
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private String getModuleName(String className) {
        if (className.contains("Task")) return "任务管理";
        if (className.contains("Wish")) return "心愿管理";
        if (className.contains("User")) return "用户管理";
        if (className.contains("Family")) return "家庭管理";
        if (className.contains("Shopping")) return "购物管理";
        if (className.contains("Diet")) return "饮食管理";
        return "其他";
    }
}