# 接口限流功能

## 简介

基于Guava RateLimiter实现的本地令牌桶限流，支持多种限流维度。

## 特性

- **全局限流**：基于方法签名限流
- **IP限流**：基于客户端IP限流
- **用户限流**：基于用户ID限流（需集成Sa-Token）
- **自定义限流**：支持自定义限流key

## 使用方法

### 1. 启用限流

限流功能默认启用，无需额外配置。

### 2. 注解方式限流

```java
@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * 全局限流，默认10 QPS
     */
    @GetMapping("/test")
    @RateLimit
    public Result<String> test() {
        return Result.success("ok");
    }

    /**
     * 按IP限流，5 QPS
     */
    @GetMapping("/test-ip")
    @RateLimit(limitType = RateLimit.LimitType.IP, permitsPerSecond = 5)
    public Result<String> testByIp() {
        return Result.success("ok");
    }

    /**
     * 按用户限流，20 QPS
     */
    @GetMapping("/test-user")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 20)
    public Result<String> testByUser() {
        return Result.success("ok");
    }

    /**
     * 自定义限流key，3 QPS
     */
    @GetMapping("/test-custom")
    @RateLimit(limitType = RateLimit.LimitType.CUSTOM, key = "custom:key", permitsPerSecond = 3)
    public Result<String> testCustom() {
        return Result.success("ok");
    }
}
```

### 3. 编程方式限流

```java
@Service
public class TestService {

    public void doSomething() {
        // 使用默认配置（10 QPS）
        if (!RateLimitUtil.tryAcquire("myKey")) {
            throw new RateLimitException("请求过于频繁");
        }
        
        // 自定义QPS
        if (!RateLimitUtil.tryAcquire("myKey", 5.0)) {
            throw new RateLimitException("请求过于频繁");
        }
    }
}
```

## 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| prefix | 限流key前缀 | rate_limit: |
| key | 限流key（支持自定义） | 空 |
| permitsPerSecond | 每秒产生的令牌数（QPS） | 10.0 |
| limitType | 限流类型（DEFAULT/IP/USER/CUSTOM） | DEFAULT |
| message | 限流提示消息 | 请求过于频繁，请稍后再试 |

## 限流异常

当触发限流时，会抛出`RateLimitException`，返回HTTP 429状态码。

```json
{
    "code": 429,
    "message": "请求过于频繁，请稍后再试",
    "data": null
}
```

## 注意事项

1. 当前实现为**本地内存限流**，适用于单机部署
2. 分布式部署建议使用Redis限流
3. 限流器在内存中以ConcurrentHashMap存储，重启后重置

