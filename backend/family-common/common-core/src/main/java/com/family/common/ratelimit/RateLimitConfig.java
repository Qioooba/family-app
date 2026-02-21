package com.family.common.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 限流配置类
 *
 * @author family
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RateLimitConfig {
}
