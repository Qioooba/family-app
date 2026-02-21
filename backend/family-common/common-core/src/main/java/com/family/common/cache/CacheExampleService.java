package com.family.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存使用示例服务
 * 演示各种缓存注解的使用方式
 *
 * @author family
 */
@Slf4j
@Service
public class CacheExampleService {

    /**
     * 示例1: 基本缓存
     * 缓存用户详情，30分钟过期
     */
    @Cacheable(value = "user", key = "#userId", expire = 30, unit = TimeUnit.MINUTES)
    public User getUserById(Long userId) {
        log.info("Query user from DB: {}", userId);
        // 模拟数据库查询
        simulateDbQuery();
        return new User(userId, "User" + userId, "user" + userId + "@example.com");
    }

    /**
     * 示例2: 组合key缓存
     * 使用SpEL表达式构建复杂key
     */
    @Cacheable(value = "user:family", key = "#familyId + ':' + #userId", expire = 1, unit = TimeUnit.HOURS)
    public User getUserByFamily(Long familyId, Long userId) {
        log.info("Query user from DB, family: {}, user: {}", familyId, userId);
        simulateDbQuery();
        return new User(userId, "FamilyUser" + userId, "user@family.com");
    }

    /**
     * 示例3: 带穿透防护的缓存
     * 开启穿透防护，缓存空值5分钟
     */
    @Cacheable(value = "user:profile", key = "#userId", 
            expire = 60, unit = TimeUnit.MINUTES,
            preventPenetration = true, nullExpire = 5)
    public UserProfile getUserProfile(Long userId) {
        log.info("Query user profile from DB: {}", userId);
        simulateDbQuery();
        // 可能返回null（用户不存在）
        if (userId < 0) {
            return null;
        }
        return new UserProfile(userId, "Profile" + userId);
    }

    /**
     * 示例4: 使用互斥锁防止缓存击穿
     * 适合热点数据
     */
    @Cacheable(value = "hot:config", key = "'global'",
            expire = 10, unit = TimeUnit.MINUTES,
            useMutex = true, mutexWaitTime = 5)
    public GlobalConfig getGlobalConfig() {
        log.info("Query global config from DB");
        simulateDbQuery();
        return new GlobalConfig("v1.0", true);
    }

    /**
     * 示例5: 条件缓存
     * 只有VIP用户才缓存
     */
    @Cacheable(value = "user:vip", key = "#userId",
            expire = 30, unit = TimeUnit.MINUTES,
            condition = "#isVip == true")
    public User getVipUser(Long userId, boolean isVip) {
        log.info("Query user from DB: {}, isVip: {}", userId, isVip);
        simulateDbQuery();
        return new User(userId, "VIP" + userId, "vip@example.com");
    }

    /**
     * 示例6: 更新缓存
     * 修改用户后更新缓存
     */
    @CachePut(value = "user", key = "#user.id", expire = 30, unit = TimeUnit.MINUTES)
    public User updateUser(User user) {
        log.info("Update user to DB: {}", user.getId());
        simulateDbQuery();
        return user;
    }

    /**
     * 示例7: 删除缓存
     * 删除用户后清除缓存
     */
    @CacheEvict(value = "user", key = "#userId")
    public void deleteUser(Long userId) {
        log.info("Delete user from DB: {}", userId);
        simulateDbQuery();
    }

    /**
     * 示例8: 批量删除缓存
     * 清除某类用户的所有缓存
     */
    @CacheEvict(value = "user:vip", allEntries = true)
    public void clearVipCache() {
        log.info("Clear all VIP user cache");
    }

    /**
     * 示例9: 方法执行前删除缓存
     */
    @CacheEvict(value = "user:profile", key = "#userId", beforeInvocation = true)
    public void refreshUserProfile(Long userId) {
        log.info("Refresh user profile: {}", userId);
        simulateDbQuery();
    }

    private void simulateDbQuery() {
        try {
            Thread.sleep(100); // 模拟100ms数据库查询
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 示例实体类
    public static class User {
        private Long id;
        private String name;
        private String email;

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

    public static class UserProfile {
        private Long userId;
        private String data;

        public UserProfile(Long userId, String data) {
            this.userId = userId;
            this.data = data;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class GlobalConfig {
        private String version;
        private boolean enabled;

        public GlobalConfig(String version, boolean enabled) {
            this.version = version;
            this.enabled = enabled;
        }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
