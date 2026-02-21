package com.family.common.cache;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类
 * 提供常用的缓存操作方法
 *
 * @author family
 */
@Component
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public CacheUtil(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    // ==================== 基础操作 ====================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    // ==================== 高级操作 ====================

    /**
     * 缓存空值（防止缓存穿透）
     */
    public void setNull(String key, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, "null", timeout, unit);
    }

    /**
     * 递增
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== 分布式锁 ====================

    /**
     * 获取分布式锁
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = getLock(lockKey);
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    /**
     * 释放锁
     */
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 使用锁执行代码
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                try {
                    return supplier.get();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    // ==================== 缓存模式 ====================

    /**
     * 缓存穿透防护 - 布隆过滤器模式
     * 先查询缓存，不存在则查询数据库并写入缓存
     */
    public <T> T getWithSet(String key, Supplier<T> dbQuery, long timeout, TimeUnit unit) {
        T value = get(key);
        if (value != null) {
            return value;
        }
        
        // 查询数据库
        value = dbQuery.get();
        if (value != null) {
            set(key, value, timeout, unit);
        }
        return value;
    }

    /**
     * 热点key保护 - 互斥锁模式
     * 防止缓存击穿
     */
    public <T> T getWithMutex(String key, Supplier<T> dbQuery, long timeout, TimeUnit unit) {
        // 先查询缓存
        T value = get(key);
        if (value != null) {
            return value;
        }
        
        // 获取分布式锁
        String lockKey = "lock:" + key;
        return executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
            // 双重检查
            T result = get(key);
            if (result != null) {
                return result;
            }
            
            // 查询数据库
            result = dbQuery.get();
            if (result != null) {
                set(key, result, timeout, unit);
            }
            return result;
        });
    }
}
