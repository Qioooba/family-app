package com.family.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 *
 * @author family
 */
@Aspect
@Component
public class DistributedLockAspect {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);
    private final RedissonClient redissonClient;
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();
    
    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint point, DistributedLock distributedLock) throws Throwable {
        String lockKey = generateLockKey(point, distributedLock);
        LockType lockType = distributedLock.type();
        
        RLock lock = getLock(lockKey, lockType);
        
        boolean acquired = false;
        try {
            // 获取锁
            long waitTime = distributedLock.waitTime();
            long leaseTime = distributedLock.leaseTime();
            TimeUnit waitUnit = distributedLock.waitTimeUnit();
            TimeUnit leaseUnit = distributedLock.leaseTimeUnit();
            
            if (distributedLock.watchDog() && leaseTime == -1) {
                // 使用看门狗自动续期
                acquired = lock.tryLock(waitTime, waitUnit);
            } else {
                acquired = lock.tryLock(waitTime, leaseTime, waitUnit);
            }
            
            if (!acquired) {
                log.warn("获取分布式锁失败，key: {}", lockKey);
                throw new LockException(distributedLock.failMessage());
            }
            
            log.debug("获取分布式锁成功，key: {}，类型: {}", lockKey, lockType);
            
            // 执行业务方法
            return point.proceed();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("获取锁被中断", e);
        } finally {
            // 释放锁
            if (acquired && distributedLock.autoUnlock() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁，key: {}", lockKey);
            }
        }
    }
    
    /**
     * 生成锁的key
     */
    private String generateLockKey(ProceedingJoinPoint point, DistributedLock distributedLock) {
        String prefix = distributedLock.prefix();
        String keyExpression = distributedLock.key();
        
        // 解析SpEL表达式
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        String[] paramNames = DISCOVERER.getParameterNames(method);
        
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        
        // 添加常用变量
        context.setVariable("method", method.getName());
        context.setVariable("class", method.getDeclaringClass().getSimpleName());
        
        Expression expression = PARSER.parseExpression(keyExpression);
        String keyValue = expression.getValue(context, String.class);
        
        return prefix + keyValue;
    }
    
    /**
     * 获取锁对象
     */
    private RLock getLock(String lockKey, LockType lockType) {
        switch (lockType) {
            case FAIR:
                return redissonClient.getFairLock(lockKey);
            case READ:
                return redissonClient.getReadWriteLock(lockKey).readLock();
            case WRITE:
                return redissonClient.getReadWriteLock(lockKey).writeLock();
            case REENTRANT:
            default:
                return redissonClient.getLock(lockKey);
        }
    }
}