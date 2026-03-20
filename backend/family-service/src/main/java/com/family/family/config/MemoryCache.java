package com.family.family.config;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存工具类
 * 用于替代 Redis，实现简单的缓存功能
 */
@Slf4j
public class MemoryCache {

    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final String value;
        final long expireTime;

        CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 设置缓存
     */
    public static void set(String key, String value, long timeoutSeconds) {
        long expireTime = timeoutSeconds > 0 ? System.currentTimeMillis() + timeoutSeconds * 1000 : Long.MAX_VALUE;
        CACHE.put(key, new CacheEntry(value, expireTime));
    }

    /**
     * 获取缓存
     */
    public static String get(String key) {
        CacheEntry entry = CACHE.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            CACHE.remove(key);
            return null;
        }
        return entry.value;
    }

    /**
     * 检查键是否存在
     */
    public static boolean hasKey(String key) {
        return get(key) != null;
    }

    /**
     * 删除缓存
     */
    public static void delete(String key) {
        CACHE.remove(key);
    }

    /**
     * 清理过期缓存（可定时调用）
     */
    public static void cleanup() {
        CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
