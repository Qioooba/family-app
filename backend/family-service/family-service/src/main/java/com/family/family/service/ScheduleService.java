package com.family.family.service;

import com.family.family.entity.Schedule;
import java.util.List;

/**
 * 日程服务接口
 */
public interface ScheduleService {

    /**
     * 创建日程
     */
    Schedule createSchedule(Schedule schedule);

    /**
     * 更新日程
     */
    Schedule updateSchedule(Schedule schedule);

    /**
     * 删除日程
     */
    void deleteSchedule(Long id);

    /**
     * 根据ID查询日程
     */
    Schedule getScheduleById(Long id);

    /**
     * 查询家庭所有日程
     */
    List<Schedule> getSchedulesByFamilyId(Long familyId);

    /**
     * 查询用户的日程
     */
    List<Schedule> getSchedulesByUserId(Long userId);
}