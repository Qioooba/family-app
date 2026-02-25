package com.family.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.NotificationSetting;

/**
 * 通知设置服务接口
 */
public interface NotificationSettingService extends IService<NotificationSetting> {
    
    /**
     * 获取用户通知设置
     */
    NotificationSetting getUserSetting(Long userId);
    
    /**
     * 获取或创建设置
     */
    NotificationSetting getOrCreateSetting(Long userId);
    
    /**
     * 更新通知设置
     */
    NotificationSetting updateSetting(Long userId, NotificationSetting setting);
    
    /**
     * 检查是否允许发送指定类型的通知
     */
    boolean isNotificationAllowed(Long userId, Integer type, Integer channel);
    
    /**
     * 检查是否在免打扰时间
     */
    boolean isInQuietTime(Long userId);
    
    /**
     * 初始化默认设置
     */
    NotificationSetting initDefaultSetting(Long userId);
}
