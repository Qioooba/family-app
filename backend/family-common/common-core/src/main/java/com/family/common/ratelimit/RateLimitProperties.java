package com.family.common.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置属性
 *
 * @author family
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "family.rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流
     */
    private boolean enabled = true;

    /**
     * 全局默认配置
     */
    private RateLimitConfig defaultConfig = new RateLimitConfig();

    /**
     * 针对特定接口的配置
     * key: 接口标识（类名:方法名）
     * value: 配置
     */
    private Map<String, RateLimitConfig> configs = new HashMap<>();

    @Data
    public static class RateLimitConfig {
        /**
         * 每秒产生的令牌数
         */
        private double permitsPerSecond = 10.0;

        /**
         * 桶容量
         */
        private long burstCapacity = 20;

        /**
         * 限流类型
         */
        private String limitType = "default";
    }
}
