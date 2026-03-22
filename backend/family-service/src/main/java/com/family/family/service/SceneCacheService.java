package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.*;
import com.family.family.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 场景缓存服务 - 替代 Redis 实现数据库存储
 */
@Slf4j
@Service
public class SceneCacheService {

    @Autowired
    private UserLocationMapper userLocationMapper;

    @Autowired
    private SceneReminderLogMapper sceneReminderLogMapper;

    @Autowired
    private UserSedentaryRecordMapper userSedentaryRecordMapper;

    @Autowired
    private UserEyeRestRecordMapper userEyeRestRecordMapper;

    @Autowired
    private UserWaterRecordMapper userWaterRecordMapper;

    // ========== 用户定位 ==========

    /**
     * 保存用户定位
     */
    public void saveUserLocation(Long userId, String location) {
        saveUserLocation(userId, location, null, null);
    }

    /**
     * 保存用户定位（含经纬度）
     */
    public void saveUserLocation(Long userId, String location, Double latitude, Double longitude) {
        UserLocation record = userLocationMapper.selectOne(
            new LambdaQueryWrapper<UserLocation>().eq(UserLocation::getUserId, userId)
        );

        if (record == null) {
            record = new UserLocation();
            record.setUserId(userId);
        } else {
        }
        record.setLocation(location);
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        if (record.getId() == null) {
            userLocationMapper.insert(record);
        } else {
            userLocationMapper.updateById(record);
        }
        log.info("保存用户定位: userId={}, location={}", userId, location);
    }

    /**
     * 获取用户定位
     */
    public String getUserLocation(Long userId) {
        UserLocation record = userLocationMapper.selectOne(
            new LambdaQueryWrapper<UserLocation>().eq(UserLocation::getUserId, userId)
        );
        return record != null ? record.getLocation() : null;
    }

    /**
     * 获取完整的用户定位记录
     */
    public UserLocation getUserLocationRecord(Long userId) {
        return userLocationMapper.selectOne(
            new LambdaQueryWrapper<UserLocation>().eq(UserLocation::getUserId, userId)
        );
    }

    // ========== 每日提醒防重复 ==========

    /**
     * 检查今日是否已提醒
     */
    public boolean hasRemindedToday(Long reminderId) {
        return sceneReminderLogMapper.checkTodayReminded(reminderId, LocalDate.now()) > 0;
    }

    /**
     * 检查今天是否已在当前计划时段提醒过。
     * 适用于固定时间类场景，避免“当天改时间后仍被旧日志锁死”。
     */
    public boolean hasRemindedForScheduledTimeToday(Long reminderId, String scheduledTime, int toleranceMinutes) {
        LocalDateTime lastTime = getLastNotificationTime(reminderId);
        if (lastTime == null || !LocalDate.now().equals(lastTime.toLocalDate())) {
            return false;
        }
        try {
            LocalTime scheduled = LocalTime.parse(scheduledTime);
            LocalTime lowerBound = scheduled.minusMinutes(toleranceMinutes);
            return !lastTime.toLocalTime().isBefore(lowerBound);
        } catch (Exception e) {
            log.warn("解析计划提醒时间失败: reminderId={}, scheduledTime={}", reminderId, scheduledTime, e);
            return hasRemindedToday(reminderId);
        }
    }

    /**
     * 记录今日已提醒
     */
    @Transactional
    public void markRemindedToday(Long reminderId, Long userId, String sceneType) {
        sceneReminderLogMapper.upsertReminderLog(reminderId, userId, sceneType, LocalDate.now());
        log.info("记录提醒: reminderId={}, sceneType={}", reminderId, sceneType);
    }

    /**
     * 获取最后一次提醒时间
     */
    public LocalDateTime getLastNotificationTime(Long reminderId) {
        return sceneReminderLogMapper.getLastReminderTime(reminderId);
    }

    /**
     * 检查是否应该再次通知（基于间隔）
     */
    public boolean shouldNotifyAgain(Long reminderId, int intervalMinutes) {
        LocalDateTime lastTime = getLastNotificationTime(reminderId);
        if (lastTime == null) {
            return true; // 从未提醒过
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLast = java.time.Duration.between(lastTime, now).toMinutes();
        boolean should = minutesSinceLast >= intervalMinutes;
        log.debug("检查间隔: reminderId={}, 上次提醒={}, 间隔={}分钟, 需要提醒={}", reminderId, lastTime, minutesSinceLast, should);
        return should;
    }

    /**
     * 记录通知（带时间戳，用于间隔提醒）
     */
    @Transactional
    public void markNotified(Long reminderId, Long userId, String sceneType) {
        sceneReminderLogMapper.upsertReminderLogWithTime(reminderId, userId, sceneType, LocalDate.now());
        log.info("记录间隔提醒: reminderId={}, sceneType={}", reminderId, sceneType);
    }

    // ========== 久坐记录 ==========

    /**
     * 获取或创建今日久坐记录
     */
    public UserSedentaryRecord getOrCreateSedentaryRecord(Long userId) {
        LocalDate today = LocalDate.now();
        UserSedentaryRecord record = userSedentaryRecordMapper.getTodayRecord(userId, today);

        if (record == null) {
            record = new UserSedentaryRecord();
            record.setUserId(userId);
            record.setRecordDate(today);
            record.setRemindCount(0);
            userSedentaryRecordMapper.insertRecord(userId, today);
            record = userSedentaryRecordMapper.getTodayRecord(userId, today);
        }
        return record;
    }

    /**
     * 获取最后久坐时间
     */
    public LocalDateTime getLastSitTime(Long userId) {
        UserSedentaryRecord record = getOrCreateSedentaryRecord(userId);
        return record != null ? record.getLastSitTime() : null;
    }

    /**
     * 更新最后久坐时间并增加提醒次数
     */
    public void updateLastSitTime(Long userId, LocalDateTime time) {
        UserSedentaryRecord record = getOrCreateSedentaryRecord(userId);
        userSedentaryRecordMapper.updateSitTime(record.getId(), time);
    }

    // ========== 护眼记录 ==========

    /**
     * 获取或创建今日护眼记录
     */
    public UserEyeRestRecord getOrCreateEyeRestRecord(Long userId) {
        LocalDate today = LocalDate.now();
        UserEyeRestRecord record = userEyeRestRecordMapper.getTodayRecord(userId, today);

        if (record == null) {
            record = new UserEyeRestRecord();
            record.setUserId(userId);
            record.setRecordDate(today);
            record.setRemindCount(0);
            userEyeRestRecordMapper.insertRecord(userId, today);
            record = userEyeRestRecordMapper.getTodayRecord(userId, today);
        }
        return record;
    }

    /**
     * 获取最后看屏幕时间
     */
    public LocalDateTime getLastScreenTime(Long userId) {
        UserEyeRestRecord record = getOrCreateEyeRestRecord(userId);
        return record != null ? record.getLastScreenTime() : null;
    }

    /**
     * 更新最后看屏幕时间并增加提醒次数
     */
    public void updateLastScreenTime(Long userId, LocalDateTime time) {
        UserEyeRestRecord record = getOrCreateEyeRestRecord(userId);
        userEyeRestRecordMapper.updateScreenTime(record.getId(), time);
    }

    // ========== 喝水记录 ==========

    /**
     * 获取或创建今日喝水记录
     */
    public UserWaterRecord getOrCreateWaterRecord(Long userId) {
        LocalDate today = LocalDate.now();
        UserWaterRecord record = userWaterRecordMapper.getTodayRecord(userId, today);

        if (record == null) {
            userWaterRecordMapper.insertRecord(userId, today);
            record = userWaterRecordMapper.getTodayRecord(userId, today);
        }
        return record;
    }

    /**
     * 获取今日喝水量
     */
    public int getTodayCupCount(Long userId) {
        UserWaterRecord record = getOrCreateWaterRecord(userId);
        return record != null && record.getCupCount() != null ? record.getCupCount() : 0;
    }

    /**
     * 增加喝水量
     */
    public void incrementCupCount(Long userId) {
        UserWaterRecord record = getOrCreateWaterRecord(userId);
        userWaterRecordMapper.incrementCupCount(record.getId());
    }
}
