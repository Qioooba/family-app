package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户活跃度
 */
@Data
@TableName("user_activity")
public class UserActivity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
    
    /**
     * 完成任务数
     */
    private Integer taskCompleted;
    
    /**
     * 饮食记录数
     */
    private Integer dietRecorded;
    
    /**
     * 活跃分钟数
     */
    private Integer activeMinutes;
}
