package com.family.notify.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.notify.entity.NotificationSetting;
import com.family.notify.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知设置控制器
 */
@RestController
@RequestMapping("/notify/settings")
@RequiredArgsConstructor
public class NotificationSettingController {
    
    private final NotificationSettingService settingService;
    
    /**
     * 获取当前用户通知设置
     */
    @GetMapping
    public Result<NotificationSetting> getUserSetting() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(settingService.getOrCreateSetting(userId));
    }
    
    /**
     * 更新通知设置
     */
    @PutMapping
    public Result<NotificationSetting> updateSetting(@RequestBody NotificationSetting setting) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(settingService.updateSetting(userId, setting));
    }
    
    /**
     * 重置为默认设置
     */
    @PostMapping("/reset")
    public Result<NotificationSetting> resetSetting() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(settingService.initDefaultSetting(userId));
    }
    
    /**
     * 检查是否在免打扰时间
     */
    @GetMapping("/quiet")
    public Result<Boolean> isInQuietTime() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(settingService.isInQuietTime(userId));
    }
}
