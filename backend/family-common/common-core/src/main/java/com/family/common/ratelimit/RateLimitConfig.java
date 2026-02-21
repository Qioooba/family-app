package com.family.common.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * 限流配置类
 *
 * @author family
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RateLimitConfig {

    /**
     * 注册限流切面
     */
    @Bean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect();
    }
}
