package com.family.family.service.impl;

import com.family.family.entity.Schedule;
import com.family.family.service.ScheduleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 排班服务实现
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    
    @Override
    public List<Schedule> list(Long familyId, String startDate, String endDate, Long userId) {
        return new ArrayList<>();
    }
    
    @Override
    public Schedule create(Schedule schedule, Long userId) {
        return schedule;
    }
    
    @Override
    public Schedule update(Schedule schedule, Long userId) {
        return schedule;
    }
    
    @Override
    public void delete(Long scheduleId, Long userId) {
    }
    
    @Override
    public List<Schedule> mySchedule(Long userId, String startDate, String endDate) {
        return new ArrayList<>();
    }
    
    @Override
    public void swap(Long scheduleId1, Long scheduleId2, Long userId) {
    }
}
