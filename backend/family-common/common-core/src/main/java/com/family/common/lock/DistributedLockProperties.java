package com.family.common.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁配置
 *
 * @author family
 */
@Component
@ConfigurationProperties(prefix = "distributed.lock")
public class DistributedLockProperties {
    
    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;
    
    /**
     * 默认锁key前缀
     */
    private String prefix = "lock:";
    
    /**
     * 默认等待时间
     */
    private long defaultWaitTime = 3;
    
    /**
     * 默认租约时间
     */
    private long defaultLeaseTime = 30;
    
    /**
     * 默认时间单位
     */
    private TimeUnit defaultTimeUnit = TimeUnit.SECONDS;
    
    /**
     * 是否打印锁日志
     */
    private boolean logEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getDefaultWaitTime() {
        return defaultWaitTime;
    }

    public void setDefaultWaitTime(long defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    public long getDefaultLeaseTime() {
        return defaultLeaseTime;
    }

    public void setDefaultLeaseTime(long defaultLeaseTime) {
        this.defaultLeaseTime = defaultLeaseTime;
    }

    public TimeUnit getDefaultTimeUnit() {
        return defaultTimeUnit;
    }

    public void setDefaultTimeUnit(TimeUnit defaultTimeUnit) {
        this.defaultTimeUnit = defaultTimeUnit;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }
}