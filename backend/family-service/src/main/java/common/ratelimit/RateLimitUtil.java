package com.family.common.ratelimit;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流工具类
 * 提供编程式限流支持
 *
 * @author family
 */
public class RateLimitUtil {

    private static final Map<String, RateLimiter> LIMITERS = new ConcurrentHashMap<>();

    /**
     * 尝试获取令牌
     *
     * @param key          限流key
     * @param permitsPerSecond 每秒产生的令牌数
     * @return 是否获取成功
     */
    public static boolean tryAcquire(String key, double permitsPerSecond) {
        RateLimiter limiter = LIMITERS.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
        return limiter.tryAcquire();
    }

    /**
     * 尝试获取令牌（使用默认配置）
     *
     * @param key 限流key
     * @return 是否获取成功
     */
    public static boolean tryAcquire(String key) {
        return tryAcquire(key, 10.0);
    }

    /**
     * 移除限流器
     *
     * @param key 限流key
     */
    public static void removeLimiter(String key) {
        LIMITERS.remove(key);
    }

    /**
     * 清空所有限流器
     */
    public static void clearAll() {
        LIMITERS.clear();
    }

    /**
     * 获取限流器（如果不存在则创建）
     *
     * @param key 限流key
     * @param permitsPerSecond 每秒产生的令牌数
     * @return RateLimiter
     */
    public static RateLimiter getLimiter(String key, double permitsPerSecond) {
        return LIMITERS.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    }
}
