package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.SystemConfig;
import com.family.family.mapper.SystemConfigMapper;
import com.family.family.service.SystemConfigService;
import com.family.family.service.WechatWorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理 - 企业微信配置等
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService configService;
    
    @Autowired
    private SystemConfigMapper configMapper;
    
    @Autowired
    private WechatWorkService wechatWorkService;

    /**
     * 获取企业微信配置状态
     */
    @GetMapping("/wechat-work/status")
    @SaCheckLogin
    public Result getWechatWorkStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("corpId", maskString(configService.getWechatWorkCorpId()));
        data.put("agentId", maskString(configService.getWechatWorkAgentId()));
        data.put("secret", maskString(configService.getWechatWorkSecret()));
        data.put("userId", configService.getWechatWorkUserId());
        data.put("isConfigured", configService.isWechatWorkConfigured());
        
        return Result.success(data);
    }

    /**
     * 更新企业微信配置
     */
    @PostMapping("/wechat-work")
    @SaCheckLogin
    public Result updateWechatWorkConfig(@RequestBody Map<String, String> params) {
        // 只有管理员可以修改
        if (!isAdmin()) {
            return Result.error("无权限");
        }
        
        String corpId = params.get("corpId");
        String agentId = params.get("agentId");
        String secret = params.get("secret");
        String userId = params.get("userId");
        
        if (corpId != null) {
            configService.setValue("wechat.work.corpid", corpId);
        }
        if (agentId != null) {
            configService.setValue("wechat.work.agentid", agentId);
        }
        if (secret != null) {
            configService.setValue("wechat.work.secret", secret);
        }
        if (userId != null) {
            configService.setValue("wechat.work.userid", userId);
        }
        
        // 刷新缓存
        configService.refreshCache();
        
        log.info("用户{}更新了企业微信配置", StpUtil.getLoginIdAsLong());
        
        return Result.success("配置已更新");
    }

    /**
     * 手动同步企业微信外部联系人
     */
    @PostMapping("/wechat-work/sync")
    @SaCheckLogin
    public Result syncExternalUsers() {
        if (!configService.isWechatWorkConfigured()) {
            return Result.error("企业微信未配置");
        }
        
        new Thread(() -> {
            wechatWorkService.syncExternalUsers();
        }).start();
        
        return Result.success("同步任务已启动");
    }

    /**
     * 获取所有配置（管理员）
     */
    @GetMapping("/list")
    @SaCheckLogin
    public Result listConfigs(@RequestParam(required = false) String category) {
        if (!isAdmin()) {
            return Result.error("无权限");
        }
        
        List<SystemConfig> configs;
        if (category != null && !category.isEmpty()) {
            configs = configMapper.selectByCategory(category);
        } else {
            configs = configMapper.selectList(null);
        }
        
        // 敏感字段脱敏
        for (SystemConfig config : configs) {
            if (config.getConfigKey().contains("secret") || config.getConfigKey().contains("password")) {
                config.setConfigValue(maskString(config.getConfigValue()));
            }
        }
        
        return Result.success(configs);
    }

    /**
     * 更新配置
     */
    @PostMapping("/update")
    @SaCheckLogin
    public Result updateConfig(@RequestBody SystemConfig config) {
        if (!isAdmin()) {
            return Result.error("无权限");
        }
        
        configService.setValue(config.getConfigKey(), config.getConfigValue());
        return Result.success("更新成功");
    }

    /**
     * 判断是否管理员
     */
    private boolean isAdmin() {
        // 这里可以根据实际情况判断，比如用户ID=1是管理员
        Long userId = StpUtil.getLoginIdAsLong();
        return userId != null && userId == 1; // 简单示例：用户ID=1是管理员
    }

    /**
     * 字符串脱敏
     */
    private String maskString(String str) {
        if (str == null || str.isEmpty()) return "";
        if (str.length() <= 4) return "****";
        return str.substring(0, 2) + "****" + str.substring(str.length() - 2);
    }
}
