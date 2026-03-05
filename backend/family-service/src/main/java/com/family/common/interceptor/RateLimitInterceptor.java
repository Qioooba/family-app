package com.family.common.interceptor;

import com.family.common.annotation.RateLimit;
import com.family.common.core.Result;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * API限流拦截器
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    // 每个接口的限流器
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        // 如果没有限流注解，放行
        if (rateLimit == null) {
            return true;
        }
        
        // 获取或创建限流器
        String key = handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName();
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(key, k -> {
            log.info("创建限流器: {}, QPS: {}", key, rateLimit.qps());
            return RateLimiter.create(rateLimit.qps());
        });
        
        // 尝试获取许可
        if (!rateLimiter.tryAcquire()) {
            log.warn("接口被限流: {}, IP: {}", key, getClientIp(request));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"" + rateLimit.message() + "\",\"data\":null}");
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取客户端IP
     */
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
        // 多个IP时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
