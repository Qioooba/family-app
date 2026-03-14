package com.family.family.service;

import com.family.family.entity.SystemConfig;
import com.family.family.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务
 * 支持从数据库读取配置，带本地缓存
 */
@Slf4j
@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigMapper configMapper;
    
    // 本地缓存
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化时加载所有配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }
    
    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        try {
            List<SystemConfig> configs = configMapper.selectList(null);
            configCache.clear();
            for (SystemConfig config : configs) {
                if (config.getConfigValue() != null) {
                    configCache.put(config.getConfigKey(), config.getConfigValue());
                }
            }
            log.info("系统配置缓存已刷新，共加载 {} 条配置", configs.size());
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
        }
    }
    
    /**
     * 获取配置值
     */
    public String getValue(String key) {
        // 先查缓存
        String value = configCache.get(key);
        if (value != null) {
            return value;
        }
        
        // 缓存未命中，查数据库
        SystemConfig config = configMapper.selectByKey(key);
        if (config != null && config.getConfigValue() != null) {
            configCache.put(key, config.getConfigValue());
            return config.getConfigValue();
        }
        
        return null;
    }
    
    /**
     * 获取配置值（带默认值）
     */
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
    
    /**
     * 更新配置
     */
    public void setValue(String key, String value) {
        SystemConfig config = configMapper.selectByKey(key);
        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCategory("general");
            configMapper.insert(config);
        }
        // 更新缓存
        configCache.put(key, value);
    }
    
    // ==================== 企业微信配置快捷方法 ====================
    
    public String getWechatWorkCorpId() {
        return getValue("wechat.work.corpid", "");
    }
    
    public String getWechatWorkAgentId() {
        return getValue("wechat.work.agentid", "");
    }
    
    public String getWechatWorkSecret() {
        return getValue("wechat.work.secret", "");
    }
    
    public String getWechatWorkUserId() {
        return getValue("wechat.work.userid", "XIAOXHUSHOU");
    }
    
    public boolean isWechatWorkConfigured() {
        return !getWechatWorkCorpId().isEmpty() 
            && !getWechatWorkAgentId().isEmpty()
            && !getWechatWorkSecret().isEmpty();
    }
}
