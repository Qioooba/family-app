package com.family.common.cache.redisson;

import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redisson延迟队列
 * 用于延迟任务处理，如订单超时取消、延迟消息等
 *
 * @author family
 */
@Component
public class RedissonDelayQueue {

    private static final Logger log = LoggerFactory.getLogger(RedissonDelayQueue.class);

    private final RedissonClient redissonClient;

    public RedissonDelayQueue(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 发送延迟消息
     *
     * @param queueName 队列名称
     * @param message 消息内容
     * @param delay 延迟时间
     * @param unit 时间单位
     * @param <T> 消息类型
     */
    public <T> void offer(String queueName, T message, long delay, TimeUnit unit) {
        RQueue<T> destinationQueue = redissonClient.getQueue(queueName);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(destinationQueue);

        delayedQueue.offer(message, delay, unit);
        log.debug("Delay message offered to queue: {}, delay: {}{}", queueName, delay, unit);
    }

    /**
     * 批量发送延迟消息
     */
    public <T> void offerAll(String queueName, Collection<T> messages, long delay, TimeUnit unit) {
        RQueue<T> destinationQueue = redissonClient.getQueue(queueName);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(destinationQueue);

        for (T message : messages) {
            delayedQueue.offer(message, delay, unit);
        }
        log.debug("Batch delay messages offered to queue: {}, count: {}, delay: {}{}",
                queueName, messages.size(), delay, unit);
    }

    /**
     * 获取目标队列（接收延迟消息）
     */
    public <T> RQueue<T> getQueue(String queueName, Class<T> clazz) {
        return redissonClient.getQueue(queueName);
    }

    /**
     * 从队列中取出消息
     */
    public <T> T poll(String queueName, Class<T> clazz) {
        RQueue<T> queue = redissonClient.getQueue(queueName);
        return queue.poll();
    }

    /**
     * 获取队列大小
     */
    public int size(String queueName) {
        RQueue<?> queue = redissonClient.getQueue(queueName);
        return queue.size();
    }

    /**
     * 清空队列
     */
    public void clear(String queueName) {
        RQueue<?> queue = redissonClient.getQueue(queueName);
        queue.clear();
        log.info("Queue cleared: {}", queueName);
    }

    /**
     * 删除队列
     */
    public boolean delete(String queueName) {
        RQueue<?> queue = redissonClient.getQueue(queueName);
        boolean deleted = queue.delete();
        if (deleted) {
            log.info("Queue deleted: {}", queueName);
        }
        return deleted;
    }
}
