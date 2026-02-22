package com.family.family.service.impl;

import com.family.family.entity.Schedule;
import com.family.family.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日程服务实现类
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    // 临时使用内存存储，后续可替换为数据库
    private final ConcurrentHashMap<Long, Schedule> scheduleMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Schedule createSchedule(Schedule schedule) {
        schedule.setId(idGenerator.getAndIncrement());
        scheduleMap.put(schedule.getId(), schedule);
        log.info("创建日程成功: {}", schedule.getId());
        return schedule;
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) {
        if (schedule.getId() == null || !scheduleMap.containsKey(schedule.getId())) {
            throw new RuntimeException("日程不存在");
        }
        scheduleMap.put(schedule.getId(), schedule);
        log.info("更新日程成功: {}", schedule.getId());
        return schedule;
    }

    @Override
    public void deleteSchedule(Long id) {
        scheduleMap.remove(id);
        log.info("删除日程成功: {}", id);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleMap.get(id);
    }

    @Override
    public List<Schedule> getSchedulesByFamilyId(Long familyId) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : scheduleMap.values()) {
            if (schedule.getFamilyId().equals(familyId)) {
                result.add(schedule);
            }
        }
        return result;
    }

    @Override
    public List<Schedule> getSchedulesByUserId(Long userId) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : scheduleMap.values()) {
            if (schedule.getUserId().equals(userId)) {
                result.add(schedule);
            }
        }
        return result;
    }
}