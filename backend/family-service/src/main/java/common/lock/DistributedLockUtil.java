package com.family.common.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 编程式锁操作
 *
 * @author family
 */
@Component
public class DistributedLockUtil {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedLockUtil.class);
    private final RedissonClient redissonClient;
    
    public DistributedLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    /**
     * 执行带锁的操作
     *
     * @param lockKey   锁key
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     * @param supplier  业务操作
     * @param <T>       返回值类型
     * @return 业务操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        return executeWithLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS, supplier);
    }
    
    /**
     * 执行带锁的操作
     *
     * @param lockKey   锁key
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     * @param timeUnit  时间单位
     * @param supplier  业务操作
     * @param <T>       返回值类型
     * @return 业务操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                throw new LockException("获取锁失败: " + lockKey);
            }
            
            log.debug("获取锁成功: {}", lockKey);
            return supplier.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 执行带锁的操作（无返回值）
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS, runnable);
    }
    
    /**
     * 执行带锁的操作（无返回值）
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable runnable) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                throw new LockException("获取锁失败: " + lockKey);
            }
            
            log.debug("获取锁成功: {}", lockKey);
            runnable.run();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 执行带锁的操作（使用看门狗自动续期）
     */
    public <T> T executeWithWatchDogLock(String lockKey, long waitTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockException("获取锁失败: " + lockKey);
            }
            
            log.debug("获取锁成功（看门狗模式）: {}", lockKey);
            return supplier.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 获取公平锁
     */
    public <T> T executeWithFairLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getFairLock(lockKey);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockException("获取公平锁失败: " + lockKey);
            }
            
            log.debug("获取公平锁成功: {}", lockKey);
            return supplier.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取公平锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放公平锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 获取读锁
     */
    public <T> T executeWithReadLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).readLock();
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockException("获取读锁失败: " + lockKey);
            }
            
            log.debug("获取读锁成功: {}", lockKey);
            return supplier.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取读锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放读锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 获取写锁
     */
    public <T> T executeWithWriteLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).writeLock();
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockException("获取写锁失败: " + lockKey);
            }
            
            log.debug("获取写锁成功: {}", lockKey);
            return supplier.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取写锁被中断: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放写锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 尝试获取锁
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 释放锁
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("释放锁: {}", lockKey);
        }
    }
    
    /**
     * 判断是否持有锁
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
    
    /**
     * 判断当前线程是否持有锁
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}