package com.family.common.cache.redisson;

import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson缓存服务
 * 封装常用缓存操作，提供统一的缓存接口
 *
 * @author family
 */
@Service
public class RedissonCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedissonCacheService.class);

    private final RedissonClient redissonClient;

    public RedissonCacheService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ==================== String 操作 ====================

    /**
     * 设置字符串缓存
     */
    public <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * 设置字符串缓存（带过期时间）
     */
    public <T> void set(String key, T value, long timeToLive, TimeUnit timeUnit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeToLive, timeUnit);
    }

    /**
     * 获取字符串缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 获取并删除
     */
    public <T> T getAndDelete(String key, Class<T> clazz) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.getAndDelete();
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        RBucket<?> bucket = redissonClient.getBucket(key);
        return bucket.delete();
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeToLive, TimeUnit timeUnit) {
        RBucket<?> bucket = redissonClient.getBucket(key);
        return bucket.expire(timeToLive, timeUnit);
    }

    // ==================== Map 操作 ====================

    /**
     * 获取Map
     */
    public <K, V> RMap<K, V> getMap(String mapName) {
        return redissonClient.getMap(mapName);
    }

    /**
     * 设置Map值
     */
    public <K, V> void setMapValue(String mapName, K key, V value) {
        RMap<K, V> map = redissonClient.getMap(mapName);
        map.put(key, value);
    }

    /**
     * 批量设置Map值
     */
    public <K, V> void setMapValues(String mapName, Map<? extends K, ? extends V> values) {
        RMap<K, V> map = redissonClient.getMap(mapName);
        map.putAll(values);
    }

    /**
     * 获取Map值
     */
    public <K, V> V getMapValue(String mapName, K key) {
        RMap<K, V> map = redissonClient.getMap(mapName);
        return map.get(key);
    }

    /**
     * 删除Map值
     */
    public <K, V> V deleteMapValue(String mapName, K key) {
        RMap<K, V> map = redissonClient.getMap(mapName);
        return map.remove(key);
    }

    /**
     * 获取Map所有值
     */
    public <K, V> Map<K, V> getMapAll(String mapName) {
        RMap<K, V> map = redissonClient.getMap(mapName);
        return map.readAllMap();
    }

    // ==================== Set 操作 ====================

    /**
     * 获取Set
     */
    public <T> RSet<T> getSet(String setName) {
        return redissonClient.getSet(setName);
    }

    /**
     * 添加Set元素
     */
    public <T> boolean addToSet(String setName, T element) {
        RSet<T> set = redissonClient.getSet(setName);
        return set.add(element);
    }

    /**
     * 批量添加Set元素
     */
    public <T> boolean addAllToSet(String setName, Collection<? extends T> elements) {
        RSet<T> set = redissonClient.getSet(setName);
        return set.addAll(elements);
    }

    /**
     * 获取Set所有元素
     */
    public <T> Set<T> getSetAll(String setName, Class<T> clazz) {
        RSet<T> set = redissonClient.getSet(setName);
        return set.readAll();
    }

    /**
     * 删除Set元素
     */
    public <T> boolean removeFromSet(String setName, T element) {
        RSet<T> set = redissonClient.getSet(setName);
        return set.remove(element);
    }

    /**
     * 判断Set是否包含元素
     */
    public <T> boolean isSetMember(String setName, T element) {
        RSet<T> set = redissonClient.getSet(setName);
        return set.contains(element);
    }

    // ==================== 通用操作 ====================

    /**
     * 判断Key是否存在
     */
    public boolean isExists(String key) {
        RBucket<?> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    /**
     * 获取Key的TTL
     */
    public long getRemainTimeToLive(String key) {
        RBucket<?> bucket = redissonClient.getBucket(key);
        return bucket.remainTimeToLive();
    }

    /**
     * 刷新过期时间
     */
    public boolean touch(String key) {
        RBucket<?> bucket = redissonClient.getBucket(key);
        return bucket.touch();
    }

    /**
     * 重命名Key
     */
    public void rename(String oldKey, String newKey) {
        RBucket<?> bucket = redissonClient.getBucket(oldKey);
        bucket.rename(newKey);
    }
}
