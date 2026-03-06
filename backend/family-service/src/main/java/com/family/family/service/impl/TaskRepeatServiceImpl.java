package com.family.family.service.impl;

import com.family.family.dto.RepeatRuleResponse;
import com.family.family.service.TaskRepeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 任务重复服务实现
 * 注意：重复功能已禁用，此服务不再执行任何操作
 */
@Service
public class TaskRepeatServiceImpl implements TaskRepeatService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskRepeatServiceImpl.class);
    
    public TaskRepeatServiceImpl() {
    }
    
    @Override
    public void setRepeatRule(Long taskId, String repeatType, String repeatRule) {
        // 重复功能已禁用，不再设置重复规则
        log.info("重复功能已禁用, taskId={}", taskId);
    }
    
    @Override
    public RepeatRuleResponse getRepeatRule(Long taskId) {
        // 重复功能已禁用，返回空数据
        return new RepeatRuleResponse(null, null);
    }
    
    /**
     * 每天凌晨2点执行，生成当日重复任务
     * 注意：重复功能已禁用，此方法不再执行任何操作
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void generateDailyRepeatingTasks() {
        // 重复功能已禁用，不再生成重复任务
        log.info("重复功能已禁用，跳过生成每日重复任务");
    }
}
