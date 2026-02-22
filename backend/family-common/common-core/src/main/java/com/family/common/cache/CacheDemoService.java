package com.family.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存示例服务
 * 演示各种缓存注解的使用方式
 *
 * @author family
 */
@Service
public class CacheDemoService {

    private static final Logger log = LoggerFactory.getLogger(CacheDemoService.class);

    /**
     * 获取用户信息（使用缓存）
     * @Cacheable: 先查缓存，无则执行方法并缓存
     */
    @Cacheable(value = "user", key = "#userId", expire = 60, unit = TimeUnit.MINUTES)
    public User getUserById(Long userId) {
        log.info("Query user from database: {}", userId);
        // 模拟数据库查询
        return new User(userId, "User" + userId, "user" + userId + "@example.com");
    }

    /**
     * 更新用户信息（更新缓存）
     * @CachePut: 执行方法并更新缓存
     */
    @CachePut(value = "user", key = "#user.id", expire = 60, unit = TimeUnit.MINUTES)
    public User updateUser(User user) {
        log.info("Update user in database: {}", user.getId());
        // 模拟数据库更新
        return user;
    }

    /**
     * 删除用户（删除缓存）
     * @CacheEvict: 执行方法并删除缓存
     */
    @CacheEvict(value = "user", key = "#userId")
    public void deleteUser(Long userId) {
        log.info("Delete user from database: {}", userId);
        // 模拟数据库删除
    }

    /**
     * 获取用户配置（使用互斥锁防止缓存击穿）
     */
    @Cacheable(value = "config", key = "#configKey", expire = 30, unit = TimeUnit.MINUTES, useMutex = true)
    public GlobalConfig getGlobalConfig(String configKey) {
        log.info("Query config from database: {}", configKey);
        // 模拟数据库查询
        return new GlobalConfig(configKey, "value_" + configKey);
    }

    /**
     * 条件缓存示例
     * 只有用户ID大于0时才缓存
     */
    @Cacheable(value = "user", key = "#userId", condition = "#userId > 0", expire = 30, unit = TimeUnit.MINUTES)
    public User getUserConditional(Long userId) {
        log.info("Query user with condition: {}", userId);
        return new User(userId, "User" + userId, "email@example.com");
    }

    // ==================== 数据模型 ====================

    public static class User {
        private Long id;
        private String name;
        private String email;

        public User() {}

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class GlobalConfig {
        private String key;
        private String value;

        public GlobalConfig() {}

        public GlobalConfig(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
