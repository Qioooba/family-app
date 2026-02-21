package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.TaskSchedule;
import com.family.family.mapper.TaskScheduleMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    
    private final TaskScheduleMapper scheduleMapper;
    
    public ScheduleController(TaskScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }
    
    @PostMapping("/create")
    public Result<Long> createSchedule(@RequestBody CreateScheduleRequest request) {
        TaskSchedule schedule = new TaskSchedule();
        schedule.setFamilyId(request.getFamilyId());
        schedule.setTaskName(request.getTaskName());
        schedule.setAssigneeId(request.getAssigneeId());
        schedule.setScheduleType(request.getScheduleType());
        schedule.setScheduleDay(request.getScheduleDay());
        schedule.setStatus(1);
        
        scheduleMapper.insert(schedule);
        return Result.success(schedule.getId());
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<TaskSchedule>> getSchedules(@PathVariable Long familyId) {
        List<TaskSchedule> list = scheduleMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskSchedule>()
                .eq(TaskSchedule::getFamilyId, familyId)
                .eq(TaskSchedule::getStatus, 1)
        );
        return Result.success(list);
    }
    
    @GetMapping("/today/{familyId}")
    public Result<List<TaskSchedule>> getTodaySchedules(@PathVariable Long familyId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();
        
        List<TaskSchedule> list = scheduleMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskSchedule>()
                .eq(TaskSchedule::getFamilyId, familyId)
                .eq(TaskSchedule::getStatus, 1)
                .eq(TaskSchedule::getScheduleDay, dayOfWeek)
        );
        return Result.success(list);
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSchedule(@PathVariable Long id) {
        scheduleMapper.deleteById(id);
        return Result.success(true);
    }
    
    public static class CreateScheduleRequest {
        private Long familyId;
        private String taskName;
        private Long assigneeId;
        private String scheduleType;
        private Integer scheduleDay;
        
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
        
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        
        public String getScheduleType() { return scheduleType; }
        public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
        
        public Integer getScheduleDay() { return scheduleDay; }
        public void setScheduleDay(Integer scheduleDay) { this.scheduleDay = scheduleDay; }
    }
}
