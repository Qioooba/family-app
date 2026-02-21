package com.family.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置属性
 *
 * @author family
 */
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RateLimitConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(RateLimitConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Map<String, RateLimitConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, RateLimitConfig> configs) {
        this.configs = configs;
    }

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

        public double getPermitsPerSecond() {
            return permitsPerSecond;
        }

        public void setPermitsPerSecond(double permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
        }

        public long getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(long burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public String getLimitType() {
            return limitType;
        }

        public void setLimitType(String limitType) {
            this.limitType = limitType;
        }
    }
}
