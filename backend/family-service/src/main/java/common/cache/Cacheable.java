package com.family.common.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解 - 简化缓存操作
 * 支持自动缓存、缓存更新、缓存删除
 *
 * @author family
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    /**
     * 缓存名称（前缀）
     */
    String value();

    /**
     * 缓存Key，支持SpEL表达式
     * 例如：#id, #user.id, #root.args[0]
     */
    String key() default "";

    /**
     * 过期时间
     */
    long expire() default 30;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * 是否开启缓存穿透防护
     */
    boolean preventPenetration() default true;

    /**
     * 空值缓存时间（用于穿透防护）
     */
    long nullExpire() default 5;

    /**
     * 是否使用互斥锁（防止缓存击穿）
     */
    boolean useMutex() default false;

    /**
     * 互斥锁等待时间（秒）
     */
    long mutexWaitTime() default 5;

    /**
     * 条件缓存，支持SpEL表达式
     * 返回true时才缓存
     */
    String condition() default "";

    /**
     * 是否缓存null值
     */
    boolean cacheNull() default false;
}
