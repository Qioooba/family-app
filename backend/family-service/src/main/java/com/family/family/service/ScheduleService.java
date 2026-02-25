package com.family.family.service;

import com.family.family.entity.Schedule;
import java.util.List;

/**
 * 排班服务接口
 */
public interface ScheduleService {

    /**
     * 获取排班列表
     */
    List<Schedule> list(Long familyId, String startDate, String endDate, Long userId);

    /**
     * 创建排班
     */
    Schedule create(Schedule schedule, Long userId);

    /**
     * 更新排班
     */
    Schedule update(Schedule schedule, Long userId);

    /**
     * 删除排班
     */
    void delete(Long scheduleId, Long userId);

    /**
     * 获取我的排班
     */
    List<Schedule> mySchedule(Long userId, String startDate, String endDate);

    /**
     * 交换排班
     */
    void swap(Long scheduleId1, Long scheduleId2, Long userId);
}