package com.family.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 分布式锁示例服务
 * 展示如何使用分布式锁
 *
 * @author family
 */
@Service
public class DistributedLockExampleService {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedLockExampleService.class);
    private final DistributedLockUtil lockUtil;
    
    public DistributedLockExampleService(DistributedLockUtil lockUtil) {
        this.lockUtil = lockUtil;
    }
    
    /**
     * 示例1：使用注解式分布式锁
     * 防止用户重复提交订单
     */
    @DistributedLock(
            key = "'order:submit:' + #userId",
            waitTime = 5,
            leaseTime = 10,
            failMessage = "订单提交中，请勿重复操作"
    )
    public String submitOrder(Long userId, String orderInfo) {
        log.info("处理用户 {} 的订单: {}", userId, orderInfo);
        // 业务逻辑...
        try {
            Thread.sleep(2000); // 模拟业务处理
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "订单提交成功";
    }
    
    /**
     * 示例2：使用注解式分布式锁 - 读写锁
     * 读操作使用读锁，支持并发读
     */
    @DistributedLock(
            key = "'inventory:' + #productId",
            type = LockType.READ,
            waitTime = 3,
            leaseTime = 5
    )
    public Integer getInventory(Long productId) {
        log.info("查询商品 {} 库存", productId);
        return 100; // 模拟查询
    }
    
    /**
     * 示例3：使用注解式分布式锁 - 写锁
     * 更新操作使用写锁，独占访问
     */
    @DistributedLock(
            key = "'inventory:' + #productId",
            type = LockType.WRITE,
            waitTime = 3,
            leaseTime = 10
    )
    public void updateInventory(Long productId, Integer quantity) {
        log.info("更新商品 {} 库存为 {}", productId, quantity);
        // 更新库存逻辑...
    }
    
    /**
     * 示例4：使用编程式锁
     * 更灵活的控制
     */
    public String deductInventory(Long productId, Integer quantity) {
        String lockKey = "inventory:deduct:" + productId;
        
        return lockUtil.executeWithLock(lockKey, 3, 10, () -> {
            log.info("扣减商品 {} 库存 {}", productId, quantity);
            // 扣减库存逻辑...
            return "扣减成功";
        });
    }
    
    /**
     * 示例5：使用公平锁
     * 按请求顺序获取锁
     */
    public String fairLockDemo(Long taskId) {
        String lockKey = "task:process:" + taskId;
        
        return lockUtil.executeWithFairLock(lockKey, 5, 30, () -> {
            log.info("公平锁处理任务 {}", taskId);
            return "处理完成";
        });
    }
    
    /**
     * 示例6：使用看门狗模式
     * 自动续期，适合执行时间不确定的任务
     */
    public String watchDogDemo(Long dataId) {
        String lockKey = "data:process:" + dataId;
        
        return lockUtil.executeWithWatchDogLock(lockKey, 5, () -> {
            log.info("看门狗模式处理数据 {}", dataId);
            // 长时间任务...
            try {
                Thread.sleep(60000); // 60秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "处理完成";
        });
    }
    
    /**
     * 示例7：双重检查锁模式
     * 防止缓存击穿
     */
    public String getDataWithDoubleCheck(Long id) {
        // 先从缓存获取
        String cacheKey = "data:" + id;
        String cachedData = getFromCache(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }
        
        // 使用分布式锁防止缓存击穿
        String lockKey = "lock:data:" + id;
        return lockUtil.executeWithLock(lockKey, 3, 10, () -> {
            // 双重检查
            String data = getFromCache(cacheKey);
            if (data != null) {
                return data;
            }
            
            // 从数据库获取
            data = loadFromDatabase(id);
            putToCache(cacheKey, data);
            return data;
        });
    }
    
    // 模拟缓存操作
    private String getFromCache(String key) {
        return null; // 模拟缓存未命中
    }
    
    private void putToCache(String key, String value) {
        // 缓存数据
    }
    
    private String loadFromDatabase(Long id) {
        log.info("从数据库加载数据 {}", id);
        return "data_" + id;
    }
}