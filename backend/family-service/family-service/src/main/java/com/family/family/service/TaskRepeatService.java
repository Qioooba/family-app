package com.family.family.service;

import com.family.family.controller.TaskController;

/**
 * 任务重复服务
 */
public interface TaskRepeatService {
    
    /**
     * 设置任务重复规则
     * @param taskId 任务ID
     * @param repeatType 重复类型: none/daily/weekly/monthly/yearly/custom
     * @param repeatRule 重复规则JSON
     */
    void setRepeatRule(Long taskId, String repeatType, String repeatRule);
    
    /**
     * 获取任务重复规则
     * @param taskId 任务ID
     * @return 重复规则响应
     */
    TaskController.RepeatRuleResponse getRepeatRule(Long taskId);
    
    /**
     * 生成今日重复任务
     * 由定时任务每天凌晨调用
     */
    void generateDailyRepeatingTasks();
}
