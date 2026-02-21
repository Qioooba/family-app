package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.dto.TaskReminderDTO;
import com.family.family.entity.Task;
import com.family.family.entity.TaskReminder;
import com.family.family.mapper.TaskMapper;
import com.family.family.mapper.TaskReminderMapper;
import com.family.family.service.TaskReminderService;
import com.family.family.vo.TaskReminderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务提醒服务实现
 */
@Service
public class TaskReminderServiceImpl implements TaskReminderService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskReminderServiceImpl.class);
    
    private final TaskReminderMapper taskReminderMapper;
    private final TaskMapper taskMapper;
    
    // 地球半径(米)
    private static final double EARTH_RADIUS = 6371000;
    // 默认地理围栏半径(米)
    private static final int DEFAULT_RADIUS = 500;
    
    public TaskReminderServiceImpl(TaskReminderMapper taskReminderMapper, TaskMapper taskMapper) {
        this.taskReminderMapper = taskReminderMapper;
        this.taskMapper = taskMapper;
    }
    
    @Override
    @Transactional
    public void setReminder(Long taskId, TaskReminderDTO dto) {
        // 验证任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 验证提醒类型
        if (!"time".equals(dto.getReminderType()) && !"location".equals(dto.getReminderType())) {
            throw new RuntimeException("无效的提醒类型，只能是time或location");
        }
        
        // 创建提醒记录
        TaskReminder reminder = new TaskReminder();
        reminder.setTaskId(taskId);
        reminder.setReminderType(dto.getReminderType());
        reminder.setIsTriggered(0);
        
        if ("time".equals(dto.getReminderType())) {
            // 时间提醒
            if (dto.getReminderTime() == null) {
                throw new RuntimeException("时间提醒需要设置提醒时间");
            }
            reminder.setReminderTime(dto.getReminderTime());
            // 更新任务的remind_time字段
            task.setRemindTime(dto.getReminderTime());
            taskMapper.updateById(task);
        } else {
            // 位置提醒
            if (dto.getLocationLat() == null || dto.getLocationLng() == null) {
                throw new RuntimeException("位置提醒需要设置经纬度");
            }
            reminder.setLocationName(dto.getLocationName());
            reminder.setLocationLat(dto.getLocationLat());
            reminder.setLocationLng(dto.getLocationLng());
            reminder.setRadius(dto.getRadius() != null ? dto.getRadius() : DEFAULT_RADIUS);
        }
        
        taskReminderMapper.insert(reminder);
        log.info("设置任务提醒成功, taskId={}, type={}", taskId, dto.getReminderType());
    }
    
    @Override
    public List<TaskReminderVO> getReminders(Long taskId) {
        List<TaskReminder> reminders = taskReminderMapper.selectList(
            new LambdaQueryWrapper<TaskReminder>()
                .eq(TaskReminder::getTaskId, taskId)
                .orderByDesc(TaskReminder::getCreateTime)
        );
        
        // 获取任务标题
        Task task = taskMapper.selectById(taskId);
        String taskTitle = task != null ? task.getTitle() : "";
        
        return reminders.stream().map(r -> convertToVO(r, taskTitle)).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteReminder(Long reminderId) {
        taskReminderMapper.deleteById(reminderId);
        log.info("删除任务提醒成功, reminderId={}", reminderId);
    }
    
    /**
     * 每分钟检查一次时间提醒
     */
    @Override
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processTimeReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(1);
        
        // 查询当前时间需要触发的提醒
        List<TaskReminder> reminders = taskReminderMapper.selectList(
            new LambdaQueryWrapper<TaskReminder>()
                .eq(TaskReminder::getReminderType, "time")
                .eq(TaskReminder::getIsTriggered, 0)
                .between(TaskReminder::getReminderTime, start, end)
        );
        
        for (TaskReminder reminder : reminders) {
            try {
                triggerReminder(reminder);
            } catch (Exception e) {
                log.error("触发时间提醒失败, reminderId={}, error={}", reminder.getId(), e.getMessage());
            }
        }
        
        if (!reminders.isEmpty()) {
            log.info("处理时间提醒完成, 触发数量={}", reminders.size());
        }
    }
    
    @Override
    @Transactional
    public void processLocationReminders(Long userId, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return;
        }
        
        // 查询该用户的所有未触发的位置提醒
        // 需要通过任务关联查询
        List<Task> userTasks = taskMapper.selectList(
            new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId)
                .ne(Task::getStatus, 2) // 未完成的任务
        );
        
        if (userTasks.isEmpty()) {
            return;
        }
        
        List<Long> taskIds = userTasks.stream().map(Task::getId).collect(Collectors.toList());
        
        List<TaskReminder> reminders = taskReminderMapper.selectList(
            new LambdaQueryWrapper<TaskReminder>()
                .eq(TaskReminder::getReminderType, "location")
                .eq(TaskReminder::getIsTriggered, 0)
                .in(TaskReminder::getTaskId, taskIds)
        );
        
        for (TaskReminder reminder : reminders) {
            try {
                if (isInGeofence(latitude, longitude, reminder)) {
                    triggerReminder(reminder);
                    log.info("触发位置提醒, reminderId={}, userId={}, location={}", 
                        reminder.getId(), userId, reminder.getLocationName());
                }
            } catch (Exception e) {
                log.error("处理位置提醒失败, reminderId={}, error={}", reminder.getId(), e.getMessage());
            }
        }
    }
    
    @Override
    public List<TaskReminderVO> getPendingTimeReminders(Long userId) {
        // 查询用户的任务
        List<Task> userTasks = taskMapper.selectList(
            new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId)
                .ne(Task::getStatus, 2)
        );
        
        if (userTasks.isEmpty()) {
            return List.of();
        }
        
        List<Long> taskIds = userTasks.stream().map(Task::getId).collect(Collectors.toList());
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查询未触发的时间提醒
        List<TaskReminder> reminders = taskReminderMapper.selectList(
            new LambdaQueryWrapper<TaskReminder>()
                .eq(TaskReminder::getReminderType, "time")
                .eq(TaskReminder::getIsTriggered, 0)
                .in(TaskReminder::getTaskId, taskIds)
                .ge(TaskReminder::getReminderTime, now)
                .orderByAsc(TaskReminder::getReminderTime)
        );
        
        return reminders.stream().map(r -> {
            Task task = userTasks.stream()
                .filter(t -> t.getId().equals(r.getTaskId()))
                .findFirst()
                .orElse(null);
            return convertToVO(r, task != null ? task.getTitle() : "");
        }).collect(Collectors.toList());
    }
    
    /**
     * 触发提醒
     */
    private void triggerReminder(TaskReminder reminder) {
        // 标记为已触发
        reminder.setIsTriggered(1);
        taskReminderMapper.updateById(reminder);
        
        // 获取任务信息
        Task task = taskMapper.selectById(reminder.getTaskId());
        if (task == null) {
            return;
        }
        
        // 这里可以调用推送服务发送通知
        // 例如：pushService.sendTaskReminder(task.getAssigneeId(), task.getTitle(), reminder);
        
        log.info("提醒已触发, reminderId={}, taskId={}, type={}", 
            reminder.getId(), reminder.getTaskId(), reminder.getReminderType());
    }
    
    /**
     * 检查是否在地理围栏内
     */
    private boolean isInGeofence(Double userLat, Double userLng, TaskReminder reminder) {
        if (reminder.getLocationLat() == null || reminder.getLocationLng() == null) {
            return false;
        }
        
        double distance = calculateDistance(
            userLat, userLng,
            reminder.getLocationLat().doubleValue(),
            reminder.getLocationLng().doubleValue()
        );
        
        int radius = reminder.getRadius() != null ? reminder.getRadius() : DEFAULT_RADIUS;
        return distance <= radius;
    }
    
    /**
     * 计算两点间距离（使用Haversine公式）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * 转换为VO
     */
    private TaskReminderVO convertToVO(TaskReminder reminder, String taskTitle) {
        TaskReminderVO vo = new TaskReminderVO();
        vo.setId(reminder.getId());
        vo.setTaskId(reminder.getTaskId());
        vo.setTaskTitle(taskTitle);
        vo.setReminderType(reminder.getReminderType());
        vo.setReminderTime(reminder.getReminderTime());
        vo.setLocationName(reminder.getLocationName());
        vo.setLocationLat(reminder.getLocationLat());
        vo.setLocationLng(reminder.getLocationLng());
        vo.setRadius(reminder.getRadius());
        vo.setIsTriggered(reminder.getIsTriggered());
        vo.setCreateTime(reminder.getCreateTime());
        return vo;
    }
}