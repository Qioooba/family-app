# 性能监控功能

## 简介

基于Micrometer + Prometheus实现的性能监控，自动采集HTTP请求指标。

## 特性

- **自动采集**：自动监控所有Controller层接口
- **多维度指标**：支持类名、方法名、URI、状态等多维度标签
- **耗时统计**：支持P50、P75、P90、P95、P99百分位统计
- **慢查询告警**：超过1秒的请求自动记录日志
- **Prometheus支持**：提供/actuator/prometheus端点

## 集成方法

### 1. 添加依赖

已在common-core中集成，服务引入common-core即可。

### 2. 配置application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    prometheus:
      enabled: true
    metrics:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles:
        http.server.requests: 0.5,0.75,0.9,0.95,0.99
      slo:
        http.server.requests: 50ms,100ms,200ms,500ms,1s,2s,5s
```

### 3. 自定义监控

```java
@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * 使用@Metrics注解添加额外监控
     */
    @GetMapping("/test")
    @Metrics(name = "custom_test", description = "自定义测试接口", slowThreshold = 500)
    public Result<String> test() {
        return Result.success("ok");
    }
}
```

## 采集指标

### 自动采集指标

| 指标名 | 类型 | 说明 |
|--------|------|------|
| http_request_duration | Timer | HTTP请求耗时（秒） |
| http_request_total | Counter | HTTP请求总数 |
| http_request_duration_ms | DistributionSummary | HTTP请求耗时（毫秒） |

### 标签说明

| 标签 | 说明 |
|------|------|
| class | 类名 |
| method | 方法名 |
| uri | 请求URI |
| status | success/error |
| error_type | 异常类型（仅失败时） |

## Prometheus配置示例

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'family-app'
    static_configs:
      - targets: ['localhost:8080', 'localhost:8081', 'localhost:8082']
    metrics_path: '/actuator/prometheus'
```

## Grafana Dashboard

### 常用查询

```promql
# QPS
rate(http_request_total[1m])

# 平均响应时间
rate(http_request_duration_sum[1m]) / rate(http_request_duration_count[1m])

# P99响应时间
histogram_quantile(0.99, rate(http_request_duration_bucket[1m]))

# 错误率
rate(http_request_total{status="error"}[1m]) / rate(http_request_total[1m])

# 各接口QPS
sum by (uri) (rate(http_request_total[1m]))
```

## 注意事项

1. 当前实现采集Controller层接口，Service层可通过@Metrics注解手动标记
2. 指标在内存中暂存，通过/actuator/prometheus暴露
3. 大量标签组合可能导致指标爆炸，建议合理控制URI数量
4. 慢查询阈值为1秒，可在@Metrics注解中自定义

