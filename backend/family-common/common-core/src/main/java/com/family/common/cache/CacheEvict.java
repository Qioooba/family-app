package com.family.common.cache;

import java.lang.annotation.*;

/**
 * 删除缓存注解
 * 用于方法执行后删除缓存
 *
 * @author family
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    /**
     * 缓存名称
     */
    String value();

    /**
     * 缓存Key，支持SpEL表达式
     */
    String key() default "";

    /**
     * 是否删除所有缓存（忽略key）
     */
    boolean allEntries() default false;

    /**
     * 是否在方法执行前删除缓存
     */
    boolean beforeInvocation() default false;
}
