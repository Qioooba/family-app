package com.family.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.cache.Cacheable;
import com.family.common.cache.CacheEvict;
import com.family.task.entity.Task;
import com.family.task.mapper.TaskMapper;
import com.family.task.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    
    @Override
    @Cacheable(value = "task", key = "#familyId + ':' + (#categoryId ?: 'all') + ':' + (#status ?: 'all')", expire = 10, unit = TimeUnit.MINUTES)
    public List<Task> getFamilyTasks(Long familyId, Long categoryId, Integer status) {
        return lambdaQuery()
                .eq(Task::getFamilyId, familyId)
                .eq(categoryId != null, Task::getCategoryId, categoryId)
                .eq(status != null, Task::getStatus, status)
                .orderByDesc(Task::getPriority)
                .orderByAsc(Task::getDueTime)
                .list();
    }
    
    @Override
    @CacheEvict(value = "task", allEntries = true)
    public Task createTask(Task task) {
        task.setStatus(0);
        save(task);
        return task;
    }
    
    @Override
    @CacheEvict(value = "task", allEntries = true)
    public Task updateTask(Task task) {
        updateById(task);
        return task;
    }
    
    @Override
    @CacheEvict(value = {"task", "task:today"}, allEntries = true)
    public void completeTask(Long taskId) {
        Task task = getById(taskId);
        task.setStatus(2);
        task.setFinishTime(LocalDateTime.now());
        updateById(task);
    }
    
    @Override
    @CacheEvict(value = "task", allEntries = true)
    public void assignTask(Long taskId, Long assigneeId) {
        Task task = getById(taskId);
        task.setAssigneeId(assigneeId);
        updateById(task);
    }
    
    @Override
    @Cacheable(value = "task:today", key = "#familyId", expire = 5, unit = TimeUnit.MINUTES)
    public List<Task> getTodayTasks(Long familyId) {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return lambdaQuery()
                .eq(Task::getFamilyId, familyId)
                .between(Task::getDueTime, start, end)
                .ne(Task::getStatus, 2)
                .orderByDesc(Task::getPriority)
                .list();
    }
}
