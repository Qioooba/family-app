package com.family.common.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * 使用Guava RateLimiter实现令牌桶限流
 *
 * @author family
 */
@Aspect
@Component
@Order(-1)
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    /**
     * 本地限流器缓存
     * key: 限流key
     * value: Guava RateLimiter
     */
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.family.common.ratelimit.RateLimit)")
    public void rateLimitPointcut() {
    }

    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 生成限流key
        String limitKey = generateKey(rateLimit, point);
        
        // 获取或创建限流器
        RateLimiter limiter = limiters.computeIfAbsent(limitKey, k -> createLimiter(rateLimit));

        // 尝试获取令牌
        boolean acquired = limiter.tryAcquire();
        
        if (acquired) {
            // 获取成功，继续执行
            return point.proceed();
        } else {
            // 获取失败，触发限流
            log.warn("触发限流: key={}, method={}, permitsPerSecond={}", 
                    limitKey, method.getName(), rateLimit.permitsPerSecond());
            throw new RateLimitException(rateLimit.message());
        }
    }

    /**
     * 创建限流器
     */
    private RateLimiter createLimiter(RateLimit rateLimit) {
        // 使用预热模式，支持突发流量
        return RateLimiter.create(rateLimit.permitsPerSecond());
    }

    /**
     * 生成限流key
     */
    private String generateKey(RateLimit rateLimit, ProceedingJoinPoint point) {
        StringBuilder key = new StringBuilder(rateLimit.prefix());
        
        switch (rateLimit.limitType()) {
            case IP:
                key.append("ip:").append(getClientIp());
                break;
            case USER:
                // TODO: 从Sa-Token获取用户ID
                key.append("user:").append(getCurrentUserId());
                break;
            case CUSTOM:
                key.append(rateLimit.key());
                break;
            case DEFAULT:
            default:
                // 默认使用类名+方法名
                key.append(point.getTarget().getClass().getSimpleName())
                   .append(":")
                   .append(point.getSignature().getName());
                break;
        }
        
        return key.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取当前用户ID
     * TODO: 集成Sa-Token后完善
     */
    private String getCurrentUserId() {
        return "anonymous";
    }
}
