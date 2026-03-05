package com.family.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 每秒允许的请求数
     */
    double qps() default 10.0;
    
    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
