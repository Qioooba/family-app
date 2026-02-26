package com.family.common.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 更新缓存注解
 * 用于方法执行后更新缓存
 *
 * @author family
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CachePut {

    /**
     * 缓存名称
     */
    String value();

    /**
     * 缓存Key，支持SpEL表达式
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
     * 条件缓存，支持SpEL表达式
     */
    String condition() default "";
    
    /**
     * 是否使用互斥锁
     */
    boolean useMutex() default false;
}
