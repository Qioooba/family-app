package com.family.common.cache.redisson;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RLongAdder;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redisson分布式计数器
 * 原子计数、HyperLogLog、LongAdder等
 *
 * @author family
 */
@Component
public class RedissonCounter {

    private final RedissonClient redissonClient;

    public RedissonCounter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ==================== 原子计数器 ====================

    /**
     * 获取当前值
     */
    public long get(String counterName) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.get();
    }

    /**
     * 设置值
     */
    public void set(String counterName, long value) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        atomicLong.set(value);
    }

    /**
     * 递增
     */
    public long increment(String counterName) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.incrementAndGet();
    }

    /**
     * 递增指定值
     */
    public long increment(String counterName, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.addAndGet(delta);
    }

    /**
     * 递减
     */
    public long decrement(String counterName) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.decrementAndGet();
    }

    /**
     * 递减指定值
     */
    public long decrement(String counterName, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.addAndGet(-delta);
    }

    /**
     * 比较并设置
     */
    public boolean compareAndSet(String counterName, long expect, long update) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.compareAndSet(expect, update);
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String counterName, long timeout, TimeUnit unit) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.expire(timeout, unit);
    }

    // ==================== LongAdder ====================

    /**
     * 创建LongAdder
     */
    public void addToLongAdder(String adderName, long value) {
        RLongAdder adder = redissonClient.getLongAdder(adderName);
        adder.add(value);
    }

    /**
     * 获取LongAdder值
     */
    public long getLongAdder(String adderName) {
        RLongAdder adder = redissonClient.getLongAdder(adderName);
        return adder.sum();
    }

    /**
     * 重置LongAdder
     */
    public void resetLongAdder(String adderName) {
        RLongAdder adder = redissonClient.getLongAdder(adderName);
        adder.reset();
    }

    // ==================== HyperLogLog ====================

    /**
     * 添加元素到HyperLogLog
     */
    public boolean addToHyperLogLog(String logName, Object... objects) {
        RHyperLogLog<Object> log = redissonClient.getHyperLogLog(logName);
        return log.add(objects);
    }

    /**
     * 获取HyperLogLog基数估算
     */
    public long countHyperLogLog(String logName) {
        RHyperLogLog<?> log = redissonClient.getHyperLogLog(logName);
        return log.count();
    }

    /**
     * 合并多个HyperLogLog
     */
    public long mergeHyperLogLogs(String targetName, String... sourceNames) {
        RHyperLogLog<Object> target = redissonClient.getHyperLogLog(targetName);
        RHyperLogLog<Object>[] sources = new RHyperLogLog[sourceNames.length];
        for (int i = 0; i < sourceNames.length; i++) {
            sources[i] = redissonClient.getHyperLogLog(sourceNames[i]);
        }
        return target.countWith(sources);
    }

    /**
     * 删除计数器
     */
    public boolean delete(String counterName) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(counterName);
        return atomicLong.delete();
    }
}
