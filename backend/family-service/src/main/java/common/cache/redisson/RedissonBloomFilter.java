package com.family.common.cache.redisson;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Redisson布隆过滤器
 * 用于防止缓存穿透，判断元素是否可能存在
 *
 * @author family
 */
@Component
public class RedissonBloomFilter {

    private static final Logger log = LoggerFactory.getLogger(RedissonBloomFilter.class);

    private final RedissonClient redissonClient;
    private final ConcurrentHashMap<String, RBloomFilter<String>> filterCache = new ConcurrentHashMap<>();

    public RedissonBloomFilter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 初始化布隆过滤器
     *
     * @param filterName 过滤器名称
     * @param expectedInsertions 预期插入数量
     * @param falseProbability 误判率（默认0.03）
     * @return 布隆过滤器实例
     */
    public RBloomFilter<String> initFilter(String filterName, long expectedInsertions, double falseProbability) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(filterName);

        if (!bloomFilter.isExists()) {
            bloomFilter.tryInit(expectedInsertions, falseProbability);
            log.info("Bloom filter initialized: {}, expectedInsertions={}, falseProbability={}",
                    filterName, expectedInsertions, falseProbability);
        }

        filterCache.put(filterName, bloomFilter);
        return bloomFilter;
    }

    /**
     * 添加元素到布隆过滤器
     */
    public boolean add(String filterName, String element) {
        RBloomFilter<String> bloomFilter = getFilter(filterName);
        if (bloomFilter != null) {
            return bloomFilter.add(element);
        }
        return false;
    }

    /**
     * 批量添加元素
     */
    public void addAll(String filterName, Iterable<String> elements) {
        RBloomFilter<String> bloomFilter = getFilter(filterName);
        if (bloomFilter != null) {
            for (String element : elements) {
                bloomFilter.add(element);
            }
        }
    }

    /**
     * 判断元素是否可能存在
     * @return true:可能存在 false:一定不存在
     */
    public boolean mightContain(String filterName, String element) {
        RBloomFilter<String> bloomFilter = getFilter(filterName);
        if (bloomFilter != null) {
            return bloomFilter.contains(element);
        }
        return true; // 过滤器不存在时，默认认为可能存在
    }

    /**
     * 获取过滤器中的元素数量
     */
    public long count(String filterName) {
        RBloomFilter<String> bloomFilter = getFilter(filterName);
        if (bloomFilter != null) {
            return bloomFilter.count();
        }
        return 0;
    }

    /**
     * 删除布隆过滤器
     */
    public boolean deleteFilter(String filterName) {
        RBloomFilter<String> bloomFilter = getFilter(filterName);
        if (bloomFilter != null) {
            boolean deleted = bloomFilter.delete();
            filterCache.remove(filterName);
            log.info("Bloom filter deleted: {}", filterName);
            return deleted;
        }
        return false;
    }

    /**
     * 获取布隆过滤器实例
     */
    private RBloomFilter<String> getFilter(String filterName) {
        RBloomFilter<String> filter = filterCache.get(filterName);
        if (filter == null) {
            filter = redissonClient.getBloomFilter(filterName);
            if (filter.isExists()) {
                filterCache.put(filterName, filter);
            } else {
                log.warn("Bloom filter not found: {}", filterName);
                return null;
            }
        }
        return filter;
    }

    /**
     * 缓存穿透防护助手类
     * 结合布隆过滤器和缓存使用
     */
    public static class PenetrationGuard {
        private final RedissonBloomFilter bloomFilter;
        private final String filterName;

        public PenetrationGuard(RedissonBloomFilter bloomFilter, String filterName, long expectedInsertions) {
            this.bloomFilter = bloomFilter;
            this.filterName = filterName;
            bloomFilter.initFilter(filterName, expectedInsertions, 0.03);
        }

        /**
         * 检查是否可能存在于缓存
         */
        public boolean mightExist(String key) {
            return bloomFilter.mightContain(filterName, key);
        }

        /**
         * 标记为已存在
         */
        public void markExists(String key) {
            bloomFilter.add(filterName, key);
        }
    }
}
