package com.family.common.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解切面
 * 处理@Cacheable、@CacheEvict、@CachePut注解
 *
 * @author family
 */
@Aspect
@Component
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

    private final CacheUtil cacheUtil;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    public CacheAspect(CacheUtil cacheUtil) {
        this.cacheUtil = cacheUtil;
    }

    /**
     * 处理@Cacheable注解
     */
    @Around("@annotation(cacheable)")
    public Object aroundCacheable(ProceedingJoinPoint point, Cacheable cacheable) throws Throwable {
        String cacheName = cacheable.value();
        String key = generateKey(point, cacheable.key());
        String fullKey = cacheName + ":" + key;

        // 检查条件
        if (!cacheable.condition().isEmpty()) {
            boolean condition = evaluateCondition(point, cacheable.condition());
            if (!condition) {
                return point.proceed();
            }
        }

        // 尝试从缓存获取
        Object cachedValue = cacheUtil.get(fullKey);
        if (cachedValue != null) {
            log.debug("Cache hit: {}", fullKey);
            return cachedValue;
        }

        log.debug("Cache miss: {}", fullKey);

        // 缓存穿透防护
        if (cacheable.preventPenetration()) {
            String nullKey = fullKey + ":null";
            Object nullValue = cacheUtil.get(nullKey);
            if (nullValue != null) {
                log.debug("Cache penetration prevented: {}", fullKey);
                return null;
            }
        }

        // 执行方法获取结果
        Object result;
        if (cacheable.useMutex()) {
            // 使用互斥锁防止缓存击穿
            result = cacheUtil.getWithMutex(fullKey, () -> {
                try {
                    return point.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }, cacheable.expire(), cacheable.unit());
        } else {
            result = point.proceed();
        }

        // 写入缓存
        if (result != null || cacheable.cacheNull()) {
            cacheUtil.set(fullKey, result, cacheable.expire(), cacheable.unit());
            log.debug("Cache set: {} expire={}{}", fullKey, cacheable.expire(), cacheable.unit());
        } else if (cacheable.preventPenetration()) {
            // 缓存空值防止穿透
            cacheUtil.set(fullKey + ":null", "null", cacheable.nullExpire(), TimeUnit.MINUTES);
        }

        return result;
    }

    /**
     * 处理@CacheEvict注解
     */
    @Around("@annotation(cacheEvict)")
    public Object aroundCacheEvict(ProceedingJoinPoint point, CacheEvict cacheEvict) throws Throwable {
        String cacheName = cacheEvict.value();

        if (cacheEvict.beforeInvocation()) {
            evictCache(point, cacheEvict, cacheName);
        }

        Object result = point.proceed();

        if (!cacheEvict.beforeInvocation()) {
            evictCache(point, cacheEvict, cacheName);
        }

        return result;
    }

    private void evictCache(ProceedingJoinPoint point, CacheEvict cacheEvict, String cacheName) {
        if (cacheEvict.allEntries()) {
            // 删除所有缓存（这里简化处理，实际可能需要扫描）
            log.debug("Cache evict all: {}", cacheName);
        } else {
            String key = generateKey(point, cacheEvict.key());
            String fullKey = cacheName + ":" + key;
            cacheUtil.delete(fullKey);
            // 同时删除空值标记
            cacheUtil.delete(fullKey + ":null");
            log.debug("Cache evict: {}", fullKey);
        }
    }

    /**
     * 处理@CachePut注解
     */
    @Around("@annotation(cachePut)")
    public Object aroundCachePut(ProceedingJoinPoint point, CachePut cachePut) throws Throwable {
        String cacheName = cachePut.value();
        String key = generateKey(point, cachePut.key());
        String fullKey = cacheName + ":" + key;

        // 执行方法
        Object result = point.proceed();

        // 检查条件
        if (!cachePut.condition().isEmpty()) {
            boolean condition = evaluateCondition(point, cachePut.condition());
            if (!condition) {
                return result;
            }
        }

        // 更新缓存
        cacheUtil.set(fullKey, result, cachePut.expire(), cachePut.unit());
        log.debug("Cache put: {} expire={}{}", fullKey, cachePut.expire(), cachePut.unit());

        return result;
    }

    /**
     * 生成缓存Key
     */
    private String generateKey(ProceedingJoinPoint point, String spel) {
        if (spel == null || spel.isEmpty()) {
            // 默认使用方法签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            return signature.getMethod().getName();
        }

        // 解析SpEL表达式
        EvaluationContext context = createContext(point);
        return parser.parseExpression(spel).getValue(context, String.class);
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(ProceedingJoinPoint point, String condition) {
        try {
            EvaluationContext context = createContext(point);
            Boolean result = parser.parseExpression(condition).getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.warn("Condition evaluation failed: {}", condition, e);
            return true; // 默认允许缓存
        }
    }

    /**
     * 创建SpEL评估上下文
     */
    private EvaluationContext createContext(ProceedingJoinPoint point) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 设置参数
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = discoverer.getParameterNames(method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 设置root对象
        context.setVariable("root", new RootObject(args, point.getTarget(), method));

        return context;
    }

    /**
     * Root对象，用于SpEL表达式
     */
    public static class RootObject {
        private final Object[] args;
        private final Object target;
        private final Method method;

        public RootObject(Object[] args, Object target, Method method) {
            this.args = args;
            this.target = target;
            this.method = method;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getTarget() {
            return target;
        }

        public Method getMethod() {
            return method;
        }
    }
}
