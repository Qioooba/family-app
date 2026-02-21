package com.family.common.ratelimit;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于令牌桶算法实现
 *
 * @author family
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String prefix() default "rate_limit:";

    /**
     * 限流key，支持SpEL表达式
     * 默认为方法签名
     */
    String key() default "";

    /**
     * 每秒产生的令牌数（QPS）
     */
    double permitsPerSecond() default 10.0;

    /**
     * 突发容量（桶大小）
     */
    long burstCapacity() default 20;

    /**
     * 限流维度
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流维度枚举
     */
    enum LimitType {
        /**
         * 默认：全局限流
         */
        DEFAULT,
        
        /**
         * 按IP限流
         */
        IP,
        
        /**
         * 按用户限流（需登录）
         */
        USER,
        
        /**
         * 自定义key
         */
        CUSTOM
    }
}
