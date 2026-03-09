package com.family.family.entity;

/**
 * 消息类型枚举
 */
public enum MessageType {
    // 任务相关
    TASK_ASSIGNED,      // 任务被指派（通知执行人）
    TASK_ASSIGN_NOTIFY, // 任务已指派（通知指派人）
    TASK_COMPLETED,     // 任务完成（通知指派人）
    TASK_COMPLETE_SELF, // 自己完成任务（通知自己）
    
    // 心愿相关（预留）
    WISH_CREATED,       // 新心愿
    WISH_COMPLETED,     // 心愿完成
    
    // 纪念日相关（预留）
    ANNIVERSARY_REMIND, // 纪念日提醒
    ANNIVERSARY_TODAY   // 今日纪念日
}
