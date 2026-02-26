package com.family.common.metrics;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 性能监控配置类
 *
 * @author family
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MetricsConfig {

    /**
     * 自定义MeterRegistry
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "family-app")
                .meterFilter(MeterFilter.deny(id -> {
                    // 过滤掉不必要的指标
                    String name = id.getName();
                    return name.startsWith("jvm.threads") || name.startsWith("jvm.gc");
                }));
    }

    /**
     * Timed切面支持（使用Micrometer的@Timed注解）
     */
    @Bean
    TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 自定义Metrics切面
     */
    @Bean
    MetricsAspect metricsAspect(MeterRegistry registry) {
        return new MetricsAspect(registry);
    }
}
