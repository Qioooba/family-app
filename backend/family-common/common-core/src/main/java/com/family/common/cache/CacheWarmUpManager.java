package com.family.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 缓存预热管理器
 * 系统启动时自动预热热点数据
 *
 * @author family
 */
@Slf4j
@Component
public class CacheWarmUpManager implements ApplicationRunner {

    private final CacheBatchUtil cacheBatchUtil;

    public CacheWarmUpManager(CacheBatchUtil cacheBatchUtil) {
        this.cacheBatchUtil = cacheBatchUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Cache warm-up manager started");

        // 异步执行预热任务
        CompletableFuture.runAsync(this::warmUpDictCache)
                .thenRunAsync(this::warmUpUserCache)
                .thenRunAsync(this::warmUpConfigCache)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Cache warm-up failed", ex);
                    } else {
                        log.info("Cache warm-up completed successfully");
                    }
                });
    }

    /**
     * 预热字典缓存
     */
    private void warmUpDictCache() {
        try {
            log.info("Warming up dict cache...");
            // 字典数据通常变化较少，适合预热
            // 实际实现需要调用字典服务获取热点数据
            Thread.sleep(100); // 模拟加载时间
            log.info("Dict cache warm-up completed");
        } catch (Exception e) {
            log.error("Dict cache warm-up failed", e);
        }
    }

    /**
     * 预热用户缓存
     */
    private void warmUpUserCache() {
        try {
            log.info("Warming up user cache...");
            // 预热活跃用户数据
            Thread.sleep(100);
            log.info("User cache warm-up completed");
        } catch (Exception e) {
            log.error("User cache warm-up failed", e);
        }
    }

    /**
     * 预热配置缓存
     */
    private void warmUpConfigCache() {
        try {
            log.info("Warming up config cache...");
            // 预热系统配置
            Thread.sleep(50);
            log.info("Config cache warm-up completed");
        } catch (Exception e) {
            log.error("Config cache warm-up failed", e);
        }
    }
}
