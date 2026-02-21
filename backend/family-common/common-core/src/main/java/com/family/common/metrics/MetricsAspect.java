package com.family.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 接口性能监控切面
 * 自动采集接口调用次数、耗时、异常等指标
 *
 * @author family
 */
@Aspect
@Component
@Order(-2)
public class MetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(MetricsAspect.class);

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 拦截Controller层方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || " +
              "@within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {
    }

    /**
     * 拦截自定义Metrics注解
     */
    @Pointcut("@annotation(com.family.common.metrics.Metrics)")
    public void metricsAnnotationPointcut() {
    }

    @Around("controllerPointcut() || metricsAnnotationPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String uri = getRequestUri();
        
        // 创建Timer.Sample用于计时
        Timer.Sample sample = Timer.start(meterRegistry);
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = point.proceed();
            
            // 记录成功指标
            recordMetrics(className, methodName, uri, sample, startTime, true, null);
            
            return result;
        } catch (Throwable throwable) {
            // 记录失败指标
            recordMetrics(className, methodName, uri, sample, startTime, false, throwable);
            throw throwable;
        }
    }

    /**
     * 记录指标
     */
    private void recordMetrics(String className, String methodName, String uri, 
                               Timer.Sample sample, long startTime, boolean success, Throwable throwable) {
        // 计算耗时
        long durationMs = System.currentTimeMillis() - startTime;
        
        // 记录Timer指标
        sample.stop(Timer.builder("http_request_duration")
                .description("HTTP请求耗时")
                .tag("class", className)
                .tag("method", methodName)
                .tag("uri", uri)
                .tag("status", success ? "success" : "error")
                .tag("error_type", throwable != null ? throwable.getClass().getSimpleName() : "none")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99)
                .register(meterRegistry));

        // 记录Counter指标（请求次数）
        meterRegistry.counter("http_request_total",
                "class", className,
                "method", methodName,
                "uri", uri,
                "status", success ? "success" : "error"
        ).increment();

        // 记录耗时分布（使用DistributionSummary）
        meterRegistry.summary("http_request_duration_ms",
                "class", className,
                "method", methodName,
                "uri", uri,
                "status", success ? "success" : "error"
        ).record(durationMs);

        // 慢查询日志（超过1秒）
        if (durationMs > 1000) {
            log.warn("慢请求告警: class={}, method={}, uri={}, duration={}ms", 
                    className, methodName, uri, durationMs);
        }
    }

    /**
     * 获取请求URI
     */
    private String getRequestUri() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                    RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            log.debug("获取请求URI失败", e);
        }
        return "unknown";
    }
}
