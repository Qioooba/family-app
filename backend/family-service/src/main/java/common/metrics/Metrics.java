package com.family.common.metrics;

import java.lang.annotation.*;

/**
 * 性能监控注解
 * 用于标记需要监控的方法
 *
 * @author family
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Metrics {

    /**
     * 监控名称
     */
    String name() default "";

    /**
     * 监控描述
     */
    String description() default "";

    /**
     * 慢查询阈值（毫秒），超过此值会记录日志
     */
    long slowThreshold() default 1000;

    /**
     * 是否记录参数
     */
    boolean logParams() default false;

    /**
     * 是否记录返回值
     */
    boolean logResult() default false;
}
