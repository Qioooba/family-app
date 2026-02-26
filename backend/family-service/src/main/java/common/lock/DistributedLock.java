package com.family.common.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 基于Redisson实现，支持多种锁类型
 *
 * @author family
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    
    /**
     * 锁的key前缀
     */
    String prefix() default "lock:";
    
    /**
     * 锁的key，支持SpEL表达式
     * 例如：#userId、#request.id、@familyLockKeyGenerator.generate(#userId, #familyId)
     */
    String key();
    
    /**
     * 锁的类型
     */
    LockType type() default LockType.REENTRANT;
    
    /**
     * 等待锁的最大时间
     */
    long waitTime() default 3;
    
    /**
     * 等待时间单位
     */
    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;
    
    /**
     * 锁的自动释放时间（租约时间）
     * 必须大于业务执行时间，防止锁提前释放
     */
    long leaseTime() default 30;
    
    /**
     * 租约时间单位
     */
    TimeUnit leaseTimeUnit() default TimeUnit.SECONDS;
    
    /**
     * 获取锁失败时的提示信息
     */
    String failMessage() default "操作过于频繁，请稍后重试";
    
    /**
     * 是否自动释放锁
     * true: 方法执行完毕后自动释放
     * false: 需要手动释放（不推荐，除非特殊场景）
     */
    boolean autoUnlock() default true;
    
    /**
     * 是否使用看门狗自动续期
     * 仅在leaseTime=-1时生效，默认30秒续期一次
     */
    boolean watchDog() default false;
}