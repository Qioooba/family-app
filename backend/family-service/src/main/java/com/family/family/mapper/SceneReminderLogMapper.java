package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.SceneReminderLog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Insert;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场景提醒日志 Mapper
 */
@Mapper
public interface SceneReminderLogMapper extends BaseMapper<SceneReminderLog> {

    /**
     * 检查今日是否已提醒
     */
    @Select("SELECT COUNT(*) FROM scene_reminder_log WHERE reminder_id = #{reminderId} AND remind_date = #{date}")
    int checkTodayReminded(@Param("reminderId") Long reminderId, @Param("date") LocalDate date);

    /**
     * 记录今日已提醒
     */
    @Insert("INSERT INTO scene_reminder_log (reminder_id, user_id, scene_type, remind_date) VALUES (#{reminderId}, #{userId}, #{sceneType}, #{date}) "
        + "ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), scene_type = VALUES(scene_type)")
    int upsertReminderLog(@Param("reminderId") Long reminderId, @Param("userId") Long userId, @Param("sceneType") String sceneType, @Param("date") LocalDate date);

    /**
     * 获取最后一次提醒时间
     */
    @Select("SELECT MAX(created_at) FROM scene_reminder_log WHERE reminder_id = #{reminderId}")
    LocalDateTime getLastReminderTime(@Param("reminderId") Long reminderId);

    /**
     * 记录提醒（使用时间戳）
     */
    @Insert("INSERT INTO scene_reminder_log (reminder_id, user_id, scene_type, remind_date, created_at) VALUES (#{reminderId}, #{userId}, #{sceneType}, #{date}, NOW()) "
        + "ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), scene_type = VALUES(scene_type), created_at = NOW()")
    int upsertReminderLogWithTime(@Param("reminderId") Long reminderId, @Param("userId") Long userId, @Param("sceneType") String sceneType, @Param("date") LocalDate date);
}
