package com.family.common.lock;

/**
 * 分布式锁类型
 *
 * @author family
 */
public enum LockType {
    
    /**
     * 可重入锁（默认）
     * 同一个线程可以多次获取锁
     */
    REENTRANT,
    
    /**
     * 公平锁
     * 按请求锁的顺序获取锁
     */
    FAIR,
    
    /**
     * 读锁
     * 共享锁，多个读线程可同时获取
     */
    READ,
    
    /**
     * 写锁
     * 独占锁，写操作独占
     */
    WRITE,
    
    /**
     * 红锁（RedLock）
     * 多Redis节点锁，防止单点故障
     */
    RED_LOCK
}