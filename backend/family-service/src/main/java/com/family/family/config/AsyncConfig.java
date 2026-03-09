package com.family.family.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 企业微信推送专用线程池
     */
    @Bean("wechatWorkExecutor")
    public Executor wechatWorkExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 核心线程数
        executor.setMaxPoolSize(20);        // 最大线程数
        executor.setQueueCapacity(500);     // 队列容量
        executor.setThreadNamePrefix("wechat-work-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
