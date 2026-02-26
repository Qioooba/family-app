package com.family.common.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存监控器
 * 统计缓存命中率、访问量等指标
 *
 * @author family
 */
@Component
public class CacheMonitor {

    private static final Logger log = LoggerFactory.getLogger(CacheMonitor.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    // 本地统计
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);

    // Micrometer指标
    private final Counter hitCounter;
    private final Counter missCounter;
    private final Timer cacheOperationTimer;

    public CacheMonitor(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;

        // 注册Micrometer指标
        this.hitCounter = Counter.builder("cache.hit")
                .description("Cache hit count")
                .register(meterRegistry);
        this.missCounter = Counter.builder("cache.miss")
                .description("Cache miss count")
                .register(meterRegistry);
        this.cacheOperationTimer = Timer.builder("cache.operation")
                .description("Cache operation duration")
                .register(meterRegistry);
    }

    /**
     * 记录缓存命中
     */
    public void recordHit() {
        hitCount.incrementAndGet();
        hitCounter.increment();
    }

    /**
     * 记录缓存未命中
     */
    public void recordMiss() {
        missCount.incrementAndGet();
        missCounter.increment();
    }

    /**
     * 记录缓存淘汰
     */
    public void recordEviction() {
        evictionCount.incrementAndGet();
        meterRegistry.counter("cache.eviction").increment();
    }

    /**
     * 记录缓存操作时间
     */
    public void recordOperationTime(long nanos) {
        cacheOperationTimer.record(nanos, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total : 0.0;

        return CacheStats.builder()
                .hitCount(hits)
                .missCount(misses)
                .evictionCount(evictionCount.get())
                .hitRate(hitRate)
                .totalRequest(total)
                .build();
    }

    /**
     * 重置统计
     */
    public void resetStats() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
    }

    /**
     * 获取Redis信息
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // 获取连接信息
            var connection = redisTemplate.getConnectionFactory().getConnection();
            var serverInfo = connection.serverCommands().info();

            if (serverInfo != null) {
                info.put("used_memory", serverInfo.getProperty("used_memory_human", "N/A"));
                info.put("connected_clients", serverInfo.getProperty("connected_clients", "0"));
                info.put("total_commands_processed", serverInfo.getProperty("total_commands_processed", "0"));
                info.put("keyspace_hits", serverInfo.getProperty("keyspace_hits", "0"));
                info.put("keyspace_misses", serverInfo.getProperty("keyspace_misses", "0"));
                info.put("uptime_in_seconds", serverInfo.getProperty("uptime_in_seconds", "0"));
            }

            // 获取Key数量
            Set<String> keys = redisTemplate.keys("*");
            info.put("key_count", keys != null ? keys.size() : 0);

        } catch (Exception e) {
            log.error("Failed to get Redis info", e);
            info.put("error", e.getMessage());
        }

        return info;
    }

    /**
     * 定期打印缓存统计
     */
    @Scheduled(fixedRate = 60000) // 每分钟
    public void printStats() {
        CacheStats stats = getStats();
        if (stats.getTotalRequest() > 0) {
            log.info("Cache stats - Hit: {}, Miss: {}, Rate: {:.2f}%",
                    stats.getHitCount(),
                    stats.getMissCount(),
                    stats.getHitRate() * 100);
        }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private long hitCount;
        private long missCount;
        private long evictionCount;
        private double hitRate;
        private long totalRequest;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private CacheStats stats = new CacheStats();

            public Builder hitCount(long count) {
                stats.hitCount = count;
                return this;
            }

            public Builder missCount(long count) {
                stats.missCount = count;
                return this;
            }

            public Builder evictionCount(long count) {
                stats.evictionCount = count;
                return this;
            }

            public Builder hitRate(double rate) {
                stats.hitRate = rate;
                return this;
            }

            public Builder totalRequest(long total) {
                stats.totalRequest = total;
                return this;
            }

            public CacheStats build() {
                return stats;
            }
        }

        // Getters
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        public double getHitRate() { return hitRate; }
        public long getTotalRequest() { return totalRequest; }
    }
}
