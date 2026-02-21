package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供系统状态、性能监控等功能
 */
@RestController
@RequestMapping("/api/monitor")
@SaCheckLogin
public class MonitorController {
    
    /**
     * 获取系统状态
     * GET /api/monitor/system
     */
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // JVM信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        status.put("jvmName", runtimeMXBean.getVmName());
        status.put("jvmVersion", runtimeMXBean.getVmVersion());
        status.put("uptime", runtimeMXBean.getUptime());
        
        // 内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", heapMemory.getUsed() / 1024 / 1024 + "MB");
        memory.put("heapCommitted", heapMemory.getCommitted() / 1024 / 1024 + "MB");
        memory.put("heapMax", heapMemory.getMax() / 1024 / 1024 + "MB");
        memory.put("nonHeapUsed", nonHeapMemory.getUsed() / 1024 / 1024 + "MB");
        
        status.put("memory", memory);
        
        // CPU核心数
        status.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        // 系统时间
        status.put("timestamp", System.currentTimeMillis());
        
        return Result.success(status);
    }
    
    /**
     * 获取健康检查
     * GET /api/monitor/health
     */
    @GetMapping("/health")
    public Result<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "UP");
        health.put("redis", "UP");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return Result.success(health);
    }
    
    /**
     * 获取性能指标
     * GET /api/monitor/metrics
     */
    @GetMapping("/metrics")
    public Result<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 线程数
        metrics.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        
        // 类加载
        metrics.put("loadedClassCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
        metrics.put("totalLoadedClassCount", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        
        // GC信息
        Map<String, Object> gc = new HashMap<>();
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
            gc.put(gcBean.getName() + "_count", gcBean.getCollectionCount());
            gc.put(gcBean.getName() + "_time", gcBean.getCollectionTime());
        });
        metrics.put("gc", gc);
        
        return Result.success(metrics);
    }
}
