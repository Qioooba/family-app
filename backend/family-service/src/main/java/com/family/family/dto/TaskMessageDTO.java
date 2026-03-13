package com.family.family.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务消息DTO - 用于企业微信推送
 */
@Data
@Builder
public class TaskMessageDTO {
    
    // 任务信息
    private Long taskId;                    // 任务ID
    private String taskTitle;               // 任务标题
    private String taskContent;             // 任务内容/备注
    private String taskStatus;              // 任务状态
    
    // 人员信息
    private String creatorName;             // 创建人
    private String assigneeName;            // 执行人
    private String operatorName;            // 操作人（完成/指派的人）
    
    // 时间信息
    private LocalDateTime createTime;       // 创建时间
    private LocalDateTime dueTime;          // 截止时间
    private LocalDateTime completeTime;     // 完成时间
    
    // 跳转链接
    private String miniProgramPath;         // 小程序页面路径
    
    /**
     * 生成小程序页面路径
     */
    public String generateMiniProgramPath() {
        if (taskId != null) {
            return String.format("pages/task/detail?id=%d&from=wechat", taskId);
        }
        return "pages/task/index";
    }
}
