package com.family.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 *
 * @author family
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int database;

    /**
     * 创建ObjectMapper（支持Java8日期时间）
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * RedisTemplate配置
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value序列化 - 使用JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(createObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisCacheManager配置（Spring Cache注解使用）
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 默认过期时间30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(createObjectMapper())))
                .disableCachingNullValues();  // 不缓存null值

        // 针对不同缓存名称的配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("user", defaultConfig.entryTtl(Duration.ofHours(1)));      // 用户缓存1小时
        configMap.put("family", defaultConfig.entryTtl(Duration.ofHours(2)));    // 家庭缓存2小时
        configMap.put("dict", defaultConfig.entryTtl(Duration.ofDays(1)));       // 字典缓存1天
        configMap.put("short", defaultConfig.entryTtl(Duration.ofMinutes(5)));   // 短缓存5分钟
        configMap.put("long", defaultConfig.entryTtl(Duration.ofDays(7)));       // 长缓存7天

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .transactionAware()
                .build();
    }

    /**
     * RedissonClient配置
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(redisPassword)
                    .setDatabase(database)
                    .setConnectionMinimumIdleSize(5)
                    .setConnectionPoolSize(20);
        } else {
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(database)
                    .setConnectionMinimumIdleSize(5)
                    .setConnectionPoolSize(20);
        }
        
        return Redisson.create(config);
    }

    /**
     * RedissonSpringCacheManager（分布式锁等高级功能使用）
     */
    @Bean
    public RedissonSpringCacheManager redissonCacheManager(RedissonClient redissonClient) {
        Map<String, org.redisson.spring.cache.CacheConfig> config = new HashMap<>();
        // 可以在这里添加Redisson特有的缓存配置
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    /**
     * 自定义Key生成器
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            for (Object param : params) {
                if (param != null) {
                    sb.append(param.toString());
                } else {
                    sb.append("null");
                }
                sb.append("_");
            }
            return sb.toString();
        };
    }
}
