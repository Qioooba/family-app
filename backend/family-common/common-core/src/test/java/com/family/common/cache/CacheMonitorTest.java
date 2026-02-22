package com.family.common.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存监控器单元测试
 */
public class CacheMonitorTest {

    @Test
    @DisplayName("测试CacheStats构建器")
    void testCacheStatsBuilder() {
        CacheMonitor.CacheStats stats = CacheMonitor.CacheStats.builder()
                .hitCount(100)
                .missCount(20)
                .evictionCount(5)
                .hitRate(0.83)
                .totalRequest(120)
                .build();

        assertEquals(100, stats.getHitCount());
        assertEquals(20, stats.getMissCount());
        assertEquals(5, stats.getEvictionCount());
        assertEquals(0.83, stats.getHitRate());
        assertEquals(120, stats.getTotalRequest());
    }

    @Test
    @DisplayName("测试CacheStats默认值")
    void testCacheStatsDefault() {
        CacheMonitor.CacheStats stats = CacheMonitor.CacheStats.builder().build();

        assertEquals(0, stats.getHitCount());
        assertEquals(0, stats.getMissCount());
        assertEquals(0.0, stats.getHitRate());
    }
}
