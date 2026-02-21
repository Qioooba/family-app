package com.family.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存批量操作工具
 * 支持批量读写、批量预热
 *
 * @author family
 */
@Slf4j
@Component
public class CacheBatchUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheBatchUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 批量获取缓存
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getBatch(Collection<K> keys, Function<K, String> keyConverter) {
        Map<K, V> result = new HashMap<>();

        // 转换key
        Map<K, String> keyMap = keys.stream()
                .collect(Collectors.toMap(k -> k, keyConverter));

        // 批量查询
        for (Map.Entry<K, String> entry : keyMap.entrySet()) {
            Object value = redisTemplate.opsForValue().get(entry.getValue());
            if (value != null) {
                result.put(entry.getKey(), (V) value);
            }
        }

        return result;
    }

    /**
     * 批量设置缓存
     */
    public <K, V> void setBatch(Map<K, V> dataMap, Function<K, String> keyConverter, long timeout, TimeUnit unit) {
        for (Map.Entry<K, V> entry : dataMap.entrySet()) {
            String key = keyConverter.apply(entry.getKey());
            redisTemplate.opsForValue().set(key, entry.getValue(), timeout, unit);
        }
        log.debug("Batch set {} cache entries", dataMap.size());
    }

    /**
     * 批量删除缓存
     */
    public <K> Long deleteBatch(Collection<K> keys, Function<K, String> keyConverter) {
        Set<String> redisKeys = keys.stream()
                .map(keyConverter)
                .collect(Collectors.toSet());
        return redisTemplate.delete(redisKeys);
    }

    /**
     * 缓存预热 - 批量加载
     * 用于系统启动时预热热点数据
     */
    public <K, V> void warmUp(Collection<K> keys, Function<Collection<K>, Map<K, V>> dbLoader,
                          Function<K, String> keyConverter, long timeout, TimeUnit unit) {
        log.info("Cache warm-up started, keys count: {}", keys.size());

        // 查询数据库
        Map<K, V> data = dbLoader.apply(keys);

        // 写入缓存
        if (data != null && !data.isEmpty()) {
            setBatch(data, keyConverter, timeout, unit);
            log.info("Cache warm-up completed, loaded {} entries", data.size());
        } else {
            log.warn("Cache warm-up: no data loaded");
        }
    }

    /**
     * 多级缓存批量获取（本地缓存 + Redis）
     * 这里预留接口，可结合Caffeine等本地缓存
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getBatchMultiLevel(Collection<K> keys, Function<K, String> keyConverter,
                                                 Function<Collection<K>, Map<K, V>> dbLoader,
                                                 long timeout, TimeUnit unit) {
        Map<K, V> result = new HashMap<>();

        // 1. 从Redis获取
        Map<K, V> redisResult = getBatch(keys, keyConverter);
        result.putAll(redisResult);

        // 2. 获取未命中的key
        Set<K> missedKeys = keys.stream()
                .filter(k -> !result.containsKey(k))
                .collect(Collectors.toSet());

        if (!missedKeys.isEmpty()) {
            log.debug("Cache miss keys: {}", missedKeys.size());

            // 3. 从数据库加载
            Map<K, V> dbResult = dbLoader.apply(missedKeys);

            // 4. 写入Redis并返回
            if (dbResult != null && !dbResult.isEmpty()) {
                setBatch(dbResult, keyConverter, timeout, unit);
                result.putAll(dbResult);
            }
        }

        return result;
    }
}
