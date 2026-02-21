package com.family.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.task.entity.Task;

import java.util.List;

public interface TaskService extends IService<Task> {
    
    List<Task> getFamilyTasks(Long familyId, Long categoryId, Integer status);
    
    Task createTask(Task task);
    
    Task updateTask(Task task);
    
    void completeTask(Long taskId);
    
    void assignTask(Long taskId, Long assigneeId);
    
    List<Task> getTodayTasks(Long familyId);
}
