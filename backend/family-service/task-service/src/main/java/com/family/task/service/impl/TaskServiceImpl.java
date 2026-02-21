package com.family.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.task.entity.Task;
import com.family.task.mapper.TaskMapper;
import com.family.task.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    
    @Override
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
    public Task createTask(Task task) {
        task.setStatus(0);
        save(task);
        return task;
    }
    
    @Override
    public Task updateTask(Task task) {
        updateById(task);
        return task;
    }
    
    @Override
    public void completeTask(Long taskId) {
        Task task = getById(taskId);
        task.setStatus(2);
        task.setFinishTime(LocalDateTime.now());
        updateById(task);
    }
    
    @Override
    public void assignTask(Long taskId, Long assigneeId) {
        Task task = getById(taskId);
        task.setAssigneeId(assigneeId);
        updateById(task);
    }
    
    @Override
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
