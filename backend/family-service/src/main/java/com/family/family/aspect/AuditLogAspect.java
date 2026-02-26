package com.family.family.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志切面
 * 记录用户操作日志
 */
@Aspect
@Component
public class AuditLogAspect {
    
    private static final Logger log = LoggerFactory.getLogger("AUDIT_LOG");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void modifyOperation() {}
    
    @Around("modifyOperation()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String ip = request != null ? getClientIp(request) : "UNKNOWN";
        
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 用户未登录
        }
        
        // 记录操作开始
        log.info("[AUDIT] 操作开始: method={}, uri={}, userId={}, ip={}, time={}",
                method, uri, userId, ip, LocalDateTime.now());
        
        Object result;
        try {
            result = point.proceed();
            
            // 记录操作成功
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AUDIT] 操作成功: method={}, uri={}, userId={}, duration={}ms",
                    method, uri, userId, duration);
            
            return result;
        } catch (Exception e) {
            // 记录操作失败
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AUDIT] 操作失败: method={}, uri={}, userId={}, duration={}ms, error={}",
                    method, uri, userId, duration, e.getMessage());
            throw e;
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
