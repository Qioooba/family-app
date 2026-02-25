package com.family.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.notify.entity.NotificationSetting;
import com.family.notify.mapper.NotificationSettingMapper;
import com.family.notify.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知设置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl extends ServiceImpl<NotificationSettingMapper, NotificationSetting> implements NotificationSettingService {
    
    @Override
    public NotificationSetting getUserSetting(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public NotificationSetting getOrCreateSetting(Long userId) {
        NotificationSetting setting = getUserSetting(userId);
        if (setting == null) {
            setting = initDefaultSetting(userId);
        }
        return setting;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationSetting updateSetting(Long userId, NotificationSetting setting) {
        NotificationSetting exist = getUserSetting(userId);
        if (exist == null) {
            // 创建新设置
            setting.setUserId(userId);
            save(setting);
            return setting;
        }
        
        // 更新设置
        setting.setId(exist.getId());
        setting.setUserId(userId);
        updateById(setting);
        return setting;
    }
    
    @Override
    public boolean isNotificationAllowed(Long userId, Integer type, Integer channel) {
        NotificationSetting setting = getOrCreateSetting(userId);
        
        // 检查是否在免打扰时间
        if (isInQuietTime(userId)) {
            // 免打扰期间，只保留站内信
            if (channel != 1) {
                return false;
            }
        }
        
        // 检查渠道是否开启
        switch (channel) {
            case 1:
                if (setting.getEnableInApp() == null || setting.getEnableInApp() == 0) return false;
                break;
            case 2:
                if (setting.getEnableEmail() == null || setting.getEnableEmail() == 0) return false;
                break;
            case 3:
                if (setting.getEnableSms() == null || setting.getEnableSms() == 0) return false;
                break;
            case 4:
                if (setting.getEnableWx() == null || setting.getEnableWx() == 0) return false;
                break;
            default:
                return false;
        }
        
        // 检查类型是否开启
        if (type != null) {
            switch (type) {
                case 1: // 系统通知
                    if (setting.getNotifySystem() == null || setting.getNotifySystem() == 0) return false;
                    break;
                case 2: // 家庭公告
                    if (setting.getNotifyAnnouncement() == null || setting.getNotifyAnnouncement() == 0) return false;
                    break;
                case 3: // 提醒
                    if (setting.getNotifyReminder() == null || setting.getNotifyReminder() == 0) return false;
                    break;
                case 4: // 活动通知
                    if (setting.getNotifyActivity() == null || setting.getNotifyActivity() == 0) return false;
                    break;
                case 5: // 任务通知
                    if (setting.getNotifyTask() == null || setting.getNotifyTask() == 0) return false;
                    break;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isInQuietTime(Long userId) {
        NotificationSetting setting = getOrCreateSetting(userId);
        
        if (setting.getEnableQuiet() == null || setting.getEnableQuiet() == 0) {
            return false;
        }
        
        String startTimeStr = setting.getQuietStartTime();
        String endTimeStr = setting.getQuietEndTime();
        
        if (startTimeStr == null || endTimeStr == null) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(startTimeStr, formatter);
            LocalTime endTime = LocalTime.parse(endTimeStr, formatter);
            LocalTime now = LocalTime.now();
            
            if (startTime.isBefore(endTime)) {
                // 同一天，如 22:00 - 08:00
                return !now.isBefore(startTime) && !now.isAfter(endTime);
            } else {
                // 跨天，如 22:00 - 08:00
                return !now.isBefore(startTime) || !now.isAfter(endTime);
            }
        } catch (Exception e) {
            log.error("解析免打扰时间失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationSetting initDefaultSetting(Long userId) {
        NotificationSetting setting = new NotificationSetting();
        setting.setUserId(userId);
        setting.setEnableInApp(1);
        setting.setEnableEmail(0);
        setting.setEnableSms(0);
        setting.setEnableWx(1);
        setting.setNotifySystem(1);
        setting.setNotifyAnnouncement(1);
        setting.setNotifyReminder(1);
        setting.setNotifyActivity(1);
        setting.setNotifyTask(1);
        setting.setEnableQuiet(0);
        setting.setQuietStartTime("22:00");
        setting.setQuietEndTime("08:00");
        
        save(setting);
        return setting;
    }
}
