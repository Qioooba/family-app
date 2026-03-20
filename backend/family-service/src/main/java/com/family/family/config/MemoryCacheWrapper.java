package com.family.family.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 内存缓存包装类
 * 替代 Redis StringRedisTemplate，用于不需要 Redis 的场景
 */
@Slf4j
@Component
public class MemoryCacheWrapper {

    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final String value;
        final long expireTime;

        CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 检查键是否存在
     */
    public Boolean hasKey(String key) {
        cleanExpired(key);
        return CACHE.containsKey(key);
    }

    /**
     * 设置值
     */
    public void opsForValue() {
        // 无需操作
    }

    /**
     * 设置值
     */
    public void set(String key, String value) {
        CACHE.put(key, new CacheEntry(value, Long.MAX_VALUE));
    }

    /**
     * 设置值并指定过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        long expireMs = timeout > 0 ? System.currentTimeMillis() + unit.toMillis(timeout) : Long.MAX_VALUE;
        CACHE.put(key, new CacheEntry(value, expireMs));
    }

    /**
     * 获取值
     */
    public String get(String key) {
        cleanExpired(key);
        CacheEntry entry = CACHE.get(key);
        return entry != null ? entry.value : null;
    }

    /**
     * 删除键
     */
    public void delete(String key) {
        CACHE.remove(key);
    }

    /**
     * 清理过期键
     */
    private void cleanExpired(String key) {
        CacheEntry entry = CACHE.get(key);
        if (entry != null && entry.isExpired()) {
            CACHE.remove(key);
        }
    }

    /**
     * 清理所有过期缓存（可定时调用）
     */
    public void cleanup() {
        CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
