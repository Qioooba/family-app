package com.family.common.cache.redisson;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RSemaphore;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redisson分布式同步工具
 * 限流器、信号量、倒计时门闩等
 *
 * @author family
 */
@Component
public class RedissonSyncTools {

    private static final Logger log = LoggerFactory.getLogger(RedissonSyncTools.class);

    private final RedissonClient redissonClient;

    public RedissonSyncTools(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ==================== 限流器 ====================

    /**
     * 初始化限流器
     *
     * @param rateLimiterName 限流器名称
     * @param rate 速率（每秒允许的请求数）
     * @return 是否初始化成功
     */
    public boolean initRateLimiter(String rateLimiterName, long rate) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        // 每秒rate个请求
        return rateLimiter.trySetRate(RateType.OVERALL, rate, 1, RateIntervalUnit.SECONDS);
    }

    /**
     * 尝试获取许可
     */
    public boolean tryAcquire(String rateLimiterName) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        return rateLimiter.tryAcquire();
    }

    /**
     * 尝试获取许可（带等待时间）
     */
    public boolean tryAcquire(String rateLimiterName, long timeout, TimeUnit unit) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        return rateLimiter.tryAcquire(1, timeout, unit);
    }

    /**
     * 获取许可（阻塞）
     */
    public void acquire(String rateLimiterName) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        rateLimiter.acquire();
    }

    // ==================== 信号量 ====================

    /**
     * 初始化信号量
     */
    public boolean initSemaphore(String semaphoreName, int permits) {
        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        return semaphore.trySetPermits(permits);
    }

    /**
     * 获取许可
     */
    public boolean tryAcquireSemaphore(String semaphoreName) throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        return semaphore.tryAcquire();
    }

    /**
     * 获取许可（带超时）
     */
    public boolean tryAcquireSemaphore(String semaphoreName, long timeout, TimeUnit unit) throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        return semaphore.tryAcquire(timeout, unit);
    }

    /**
     * 释放许可
     */
    public void releaseSemaphore(String semaphoreName) {
        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        semaphore.release();
    }

    /**
     * 获取可用许可数
     */
    public int availablePermits(String semaphoreName) {
        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        return semaphore.availablePermits();
    }

    // ==================== 倒计时门闩 ====================

    /**
     * 初始化倒计时门闩
     */
    public void initCountDownLatch(String latchName, int count) {
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchName);
        latch.trySetCount(count);
    }

    /**
     * 等待倒计时
     */
    public void awaitCountDown(String latchName) throws InterruptedException {
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchName);
        latch.await();
    }

    /**
     * 等待倒计时（带超时）
     */
    public boolean awaitCountDown(String latchName, long timeout, TimeUnit unit) throws InterruptedException {
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchName);
        return latch.await(timeout, unit);
    }

    /**
     * 计数减一
     */
    public void countDown(String latchName) {
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchName);
        latch.countDown();
    }

    /**
     * 获取当前计数
     */
    public long getCount(String latchName) {
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchName);
        return latch.getCount();
    }

    /**
     * API限流助手
     * 简化限流操作
     */
    public static class RateLimitHelper {
        private final RedissonSyncTools syncTools;
        private final String limiterName;

        public RateLimitHelper(RedissonSyncTools syncTools, String limiterName, long rate) {
            this.syncTools = syncTools;
            this.limiterName = limiterName;
            syncTools.initRateLimiter(limiterName, rate);
        }

        /**
         * 执行限流操作
         * @return true:获得许可 false:被限流
         */
        public boolean tryExecute() {
            return syncTools.tryAcquire(limiterName);
        }

        /**
         * 执行限流操作（阻塞等待）
         */
        public void execute() {
            syncTools.acquire(limiterName);
        }
    }
}
