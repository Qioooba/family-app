package com.family.common.cache;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理API
 * 提供缓存监控、管理接口
 *
 * @author family
 */
@RestController
@RequestMapping("/api/cache/admin")
@SaCheckLogin
public class CacheAdminController {

    private static final Logger log = LoggerFactory.getLogger(CacheAdminController.class);

    private final CacheMonitor cacheMonitor;

    public CacheAdminController(CacheMonitor cacheMonitor) {
        this.cacheMonitor = cacheMonitor;
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> result = new HashMap<>();

        CacheMonitor.CacheStats stats = cacheMonitor.getStats();
        result.put("hitCount", stats.getHitCount());
        result.put("missCount", stats.getMissCount());
        result.put("evictionCount", stats.getEvictionCount());
        result.put("hitRate", stats.getHitRate());
        result.put("totalRequest", stats.getTotalRequest());

        return result;
    }

    /**
     * 获取Redis信息
     */
    @GetMapping("/redis/info")
    public Map<String, Object> getRedisInfo() {
        return cacheMonitor.getRedisInfo();
    }

    /**
     * 重置缓存统计
     */
    @PostMapping("/stats/reset")
    public Map<String, String> resetStats() {
        cacheMonitor.resetStats();
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Cache stats reset successfully");
        return result;
    }
}
