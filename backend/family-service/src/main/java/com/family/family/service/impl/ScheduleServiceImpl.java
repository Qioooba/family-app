package com.family.family.service.impl;

import com.family.family.entity.Schedule;
import com.family.family.mapper.ScheduleMapper;
import com.family.family.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 排班服务实现类
 * 使用数据库存储，支持分布式部署
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public List<Schedule> list(Long familyId, String startDate, String endDate, Long userId) {
        if (familyId == null) {
            log.warn("listSchedules: familyId is null");
            return List.of();
        }
        return scheduleMapper.selectByFamilyId(familyId);
    }

    @Override
    public Schedule create(Schedule schedule, Long userId) {
        if (schedule == null || schedule.getFamilyId() == null) {
            throw new RuntimeException("家庭ID不能为空");
        }
        schedule.setCreatorId(userId);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);
        log.info("创建排班成功: {}", schedule.getId());
        return schedule;
    }

    @Override
    public Schedule update(Schedule schedule, Long userId) {
        if (schedule.getId() == null) {
            throw new RuntimeException("排班ID不能为空");
        }
        Schedule existing = scheduleMapper.selectById(schedule.getId());
        if (existing == null) {
            throw new RuntimeException("排班不存在");
        }
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);
        log.info("更新排班成功: {}", schedule.getId());
        return schedule;
    }

    @Override
    public void delete(Long scheduleId, Long userId) {
        Schedule existing = scheduleMapper.selectById(scheduleId);
        if (existing == null) {
            throw new RuntimeException("排班不存在");
        }
        scheduleMapper.deleteById(scheduleId);
        log.info("删除排班成功: {}", scheduleId);
    }

    @Override
    public Schedule getById(Long scheduleId) {
        return scheduleMapper.selectById(scheduleId);
    }

    @Override
    public List<Schedule> getTodaySchedule(Long familyId, Long userId) {
        String today = LocalDate.now().toString();
        return scheduleMapper.selectByFamilyAndUserAndDate(familyId, userId, today);
    }

    @Override
    public List<Schedule> mySchedule(Long userId, String startDate, String endDate) {
        return scheduleMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getStatus, 1)
                .orderByAsc(Schedule::getDayOfWeek)
        );
    }

    @Override
    public void swap(Long scheduleId1, Long scheduleId2, Long userId) {
        Schedule s1 = scheduleMapper.selectById(scheduleId1);
        Schedule s2 = scheduleMapper.selectById(scheduleId2);
        
        if (s1 == null || s2 == null) {
            throw new RuntimeException("排班不存在");
        }
        
        // 交换 userId
        Long tempUserId = s1.getUserId();
        s1.setUserId(s2.getUserId());
        s2.setUserId(tempUserId);
        
        s1.setUpdateTime(LocalDateTime.now());
        s2.setUpdateTime(LocalDateTime.now());
        
        scheduleMapper.updateById(s1);
        scheduleMapper.updateById(s2);
        
        log.info("交换排班成功: {} <-> {}", scheduleId1, scheduleId2);
    }
}
