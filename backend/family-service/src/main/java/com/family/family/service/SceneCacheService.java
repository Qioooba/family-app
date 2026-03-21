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
        UserLocation record = userLocationMapper.selectOne(
            new LambdaQueryWrapper<UserLocation>().eq(UserLocation::getUserId, userId)
        );

        if (record == null) {
            record = new UserLocation();
            record.setUserId(userId);
            record.setLocation(location);
            userLocationMapper.insert(record);
        } else {
            record.setLocation(location);
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

    // ========== 每日提醒防重复 ==========

    /**
     * 检查今日是否已提醒
     */
    public boolean hasRemindedToday(Long reminderId) {
        return sceneReminderLogMapper.checkTodayReminded(reminderId, LocalDate.now()) > 0;
    }

    /**
     * 记录今日已提醒
     */
    @Transactional
    public void markRemindedToday(Long reminderId, Long userId, String sceneType) {
        if (!hasRemindedToday(reminderId)) {
            sceneReminderLogMapper.insertReminderLog(reminderId, userId, sceneType, LocalDate.now());
            log.info("记录提醒: reminderId={}, sceneType={}", reminderId, sceneType);
        }
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
