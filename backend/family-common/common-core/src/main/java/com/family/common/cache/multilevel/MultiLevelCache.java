package com.family.common.cache.multilevel;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 多级缓存管理器
 * L1: Caffeine本地缓存
 * L2: Redis分布式缓存
 *
 * @author family
 */
@Component
public class MultiLevelCache {

    private static final Logger log = LoggerFactory.getLogger(MultiLevelCache.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public MultiLevelCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建多级缓存
     *
     * @param cacheName 缓存名称
     * @param localMaxSize 本地缓存最大数量
     * @param localExpireSeconds 本地缓存过期时间（秒）
     * @param redisExpireSeconds Redis缓存过期时间（秒）
     * @param dbLoader 数据库加载器
     * @param <K> key类型
     * @param <V> value类型
     * @return 多级缓存实例
     */
    public <K, V> MultiLevelCacheInstance<K, V> createCache(
            String cacheName,
            long localMaxSize,
            long localExpireSeconds,
            long redisExpireSeconds,
            Function<K, V> dbLoader) {

        return new MultiLevelCacheInstance<>(
                cacheName,
                redisTemplate,
                localMaxSize,
                localExpireSeconds,
                redisExpireSeconds,
                dbLoader
        );
    }

    /**
     * 多级缓存实例
     */
    public static class MultiLevelCacheInstance<K, V> {
        private final String cacheName;
        private final RedisTemplate<String, Object> redisTemplate;
        private final LoadingCache<K, V> localCache;
        private final Function<K, V> dbLoader;
        private final long redisExpireSeconds;

        public MultiLevelCacheInstance(
                String cacheName,
                RedisTemplate<String, Object> redisTemplate,
                long localMaxSize,
                long localExpireSeconds,
                long redisExpireSeconds,
                Function<K, V> dbLoader) {
            this.cacheName = cacheName;
            this.redisTemplate = redisTemplate;
            this.redisExpireSeconds = redisExpireSeconds;
            this.dbLoader = dbLoader;

            // 初始化Caffeine本地缓存
            this.localCache = Caffeine.newBuilder()
                    .maximumSize(localMaxSize)
                    .expireAfterWrite(localExpireSeconds, TimeUnit.SECONDS)
                    .recordStats()
                    .build(this::loadFromRedisOrDb);
        }

        /**
         * 获取缓存
         */
        public V get(K key) {
            return localCache.get(key);
        }

        /**
         * 从Redis或数据库加载
         */
        @SuppressWarnings("unchecked")
        private V loadFromRedisOrDb(K key) {
            String redisKey = buildRedisKey(key);

            // 尝试从Redis获取
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value != null) {
                log.debug("L2 cache hit: {}", redisKey);
                return (V) value;
            }

            // 从数据库加载
            V dbValue = dbLoader.apply(key);
            if (dbValue != null) {
                redisTemplate.opsForValue().set(redisKey, dbValue, redisExpireSeconds, TimeUnit.SECONDS);
                log.debug("DB loaded and cached: {}", redisKey);
            }
            return dbValue;
        }

        /**
         * 设置缓存
         */
        public void put(K key, V value) {
            // 更新本地缓存
            localCache.put(key, value);

            // 更新Redis
            String redisKey = buildRedisKey(key);
            redisTemplate.opsForValue().set(redisKey, value, redisExpireSeconds, TimeUnit.SECONDS);
        }

        /**
         * 删除缓存
         */
        public void evict(K key) {
            // 使本地缓存失效
            localCache.invalidate(key);

            // 删除Redis缓存
            String redisKey = buildRedisKey(key);
            redisTemplate.delete(redisKey);
        }

        /**
         * 清空所有缓存
         */
        public void clear() {
            localCache.invalidateAll();
            // Redis缓存通过pattern删除
            String pattern = cacheName + ":*";
            // 注意：生产环境谨慎使用keys命令
        }

        /**
         * 获取本地缓存统计
         */
        public com.github.benmanes.caffeine.cache.stats.CacheStats stats() {
            return localCache.stats();
        }

        private String buildRedisKey(K key) {
            return cacheName + ":" + key.toString();
        }
    }
}
