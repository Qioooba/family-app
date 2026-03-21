package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.SceneReminderLog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Insert;

import java.time.LocalDate;

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
    @Insert("INSERT INTO scene_reminder_log (reminder_id, user_id, scene_type, remind_date) VALUES (#{reminderId}, #{userId}, #{sceneType}, #{date})")
    int insertReminderLog(@Param("reminderId") Long reminderId, @Param("userId") Long userId, @Param("sceneType") String sceneType, @Param("date") LocalDate date);
}
