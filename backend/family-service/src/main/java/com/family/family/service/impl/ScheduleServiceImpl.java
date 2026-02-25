package com.family.family.service.impl;

import com.family.family.entity.Schedule;
import com.family.family.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 排班服务实现类
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    // 临时使用内存存储，后续可替换为数据库
    private final ConcurrentHashMap<Long, Schedule> scheduleMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Schedule> list(Long familyId, String startDate, String endDate, Long userId) {
        return scheduleMap.values().stream()
                .filter(s -> s.getFamilyId().equals(familyId))
                .collect(Collectors.toList());
    }

    @Override
    public Schedule create(Schedule schedule, Long userId) {
        schedule.setId(idGenerator.getAndIncrement());
        schedule.setCreatorId(userId);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());
        schedule.setStatus(1);
        scheduleMap.put(schedule.getId(), schedule);
        log.info("创建排班成功: {}", schedule.getId());
        return schedule;
    }

    @Override
    public Schedule update(Schedule schedule, Long userId) {
        if (schedule.getId() == null || !scheduleMap.containsKey(schedule.getId())) {
            throw new RuntimeException("排班不存在");
        }
        Schedule existing = scheduleMap.get(schedule.getId());
        existing.setUserId(schedule.getUserId());
        existing.setTaskName(schedule.getTaskName());
        existing.setDayOfWeek(schedule.getDayOfWeek());
        existing.setStartDate(schedule.getStartDate());
        existing.setEndDate(schedule.getEndDate());
        existing.setUpdateTime(LocalDateTime.now());
        scheduleMap.put(schedule.getId(), existing);
        log.info("更新排班成功: {}", schedule.getId());
        return existing;
    }

    @Override
    public void delete(Long scheduleId, Long userId) {
        scheduleMap.remove(scheduleId);
        log.info("删除排班成功: {}", scheduleId);
    }

    @Override
    public List<Schedule> mySchedule(Long userId, String startDate, String endDate) {
        return scheduleMap.values().stream()
                .filter(s -> s.getUserId() != null && s.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void swap(Long scheduleId1, Long scheduleId2, Long userId) {
        Schedule s1 = scheduleMap.get(scheduleId1);
        Schedule s2 = scheduleMap.get(scheduleId2);
        if (s1 == null || s2 == null) {
            throw new RuntimeException("排班不存在");
        }
        Long tempUserId = s1.getUserId();
        s1.setUserId(s2.getUserId());
        s2.setUserId(tempUserId);
        s1.setUpdateTime(LocalDateTime.now());
        s2.setUpdateTime(LocalDateTime.now());
        log.info("交换排班成功: {} <-> {}", scheduleId1, scheduleId2);
    }
}