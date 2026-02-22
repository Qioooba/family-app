package com.family.common.cache.redisson;

import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Redisson发布订阅
 * 用于缓存同步、消息广播等场景
 *
 * @author family
 */
@Component
public class RedissonPubSub {

    private static final Logger log = LoggerFactory.getLogger(RedissonPubSub.class);

    private final RedissonClient redissonClient;
    private final ConcurrentHashMap<String, Integer> listenerMap = new ConcurrentHashMap<>();

    public RedissonPubSub(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 发布消息
     *
     * @param topic 主题名称
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 接收消息的客户端数量
     */
    public <T> long publish(String topic, T message) {
        RTopic<T> rTopic = redissonClient.getTopic(topic);
        long clients = rTopic.publish(message);
        log.debug("Message published to topic: {}, clients: {}", topic, clients);
        return clients;
    }

    /**
     * 订阅消息
     *
     * @param topic 主题名称
     * @param listener 消息监听器
     * @param <T> 消息类型
     * @return 监听器ID
     */
    public <T> int subscribe(String topic, MessageListener<T> listener) {
        RTopic<T> rTopic = redissonClient.getTopic(topic);
        int listenerId = rTopic.addListener(listener);
        listenerMap.put(topic + ":" + listenerId, listenerId);
        log.info("Subscribed to topic: {}, listenerId: {}", topic, listenerId);
        return listenerId;
    }

    /**
     * 取消订阅
     */
    public void unsubscribe(String topic, int listenerId) {
        RTopic<?> rTopic = redissonClient.getTopic(topic);
        rTopic.removeListener(listenerId);
        listenerMap.remove(topic + ":" + listenerId);
        log.info("Unsubscribed from topic: {}, listenerId: {}", topic, listenerId);
    }

    /**
     * 缓存同步消息
     */
    public static class CacheSyncMessage {
        private String cacheName;
        private String key;
        private SyncAction action;
        private Object value;
        private long timestamp;

        public CacheSyncMessage() {}

        public CacheSyncMessage(String cacheName, String key, SyncAction action) {
            this.cacheName = cacheName;
            this.key = key;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }

        public static CacheSyncMessage evict(String cacheName, String key) {
            return new CacheSyncMessage(cacheName, key, SyncAction.EVICT);
        }

        public static CacheSyncMessage put(String cacheName, String key, Object value) {
            CacheSyncMessage msg = new CacheSyncMessage(cacheName, key, SyncAction.PUT);
            msg.setValue(value);
            return msg;
        }

        public static CacheSyncMessage clear(String cacheName) {
            return new CacheSyncMessage(cacheName, "*", SyncAction.CLEAR);
        }

        // Getters and Setters
        public String getCacheName() { return cacheName; }
        public void setCacheName(String cacheName) { this.cacheName = cacheName; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public SyncAction getAction() { return action; }
        public void setAction(SyncAction action) { this.action = action; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public enum SyncAction {
        PUT,    // 设置缓存
        EVICT,  // 删除缓存
        CLEAR   // 清空缓存
    }

    /**
     * 缓存同步发布器
     */
    @Component
    public static class CacheSyncPublisher {
        private final RedissonPubSub pubSub;
        private static final String CACHE_SYNC_TOPIC = "cache:sync";

        public CacheSyncPublisher(RedissonPubSub pubSub) {
            this.pubSub = pubSub;
        }

        /**
         * 发布缓存删除消息
         */
        public void publishEvict(String cacheName, String key) {
            pubSub.publish(CACHE_SYNC_TOPIC, CacheSyncMessage.evict(cacheName, key));
        }

        /**
         * 发布缓存更新消息
         */
        public void publishPut(String cacheName, String key, Object value) {
            pubSub.publish(CACHE_SYNC_TOPIC, CacheSyncMessage.put(cacheName, key, value));
        }

        /**
         * 发布缓存清空消息
         */
        public void publishClear(String cacheName) {
            pubSub.publish(CACHE_SYNC_TOPIC, CacheSyncMessage.clear(cacheName));
        }
    }
}
