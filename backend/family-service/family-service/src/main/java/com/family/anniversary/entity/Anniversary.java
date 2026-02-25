package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("anniversary")
public class Anniversary {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    
    private Long userId;
    
    private String title;
    
    /**
     * 纪念日类型: birthday, love, wedding, family, traditional, custom
     */
    private String type;
    
    /**
     * 日期类型: solar-阳历, lunar-农历
     */
    private String dateType;
    
    /**
     * 目标日期
     */
    private LocalDate targetDate;
    
    /**
     * 是否每年重复
     */
    private Integer isRecurring;
    
    /**
     * 提前提醒天数 [1,3,7]
     */
    private String reminderDays;
    
    /**
     * 关联用户ID
     */
    private Long relatedUserId;
    
    private String description;
    
    /**
     * 纪念相册JSON
     */
    private String images;
    
    /**
     * 图标
     */
    private String icon;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 非数据库字段：倒计时天数
     */
    @TableField(exist = false)
    private Integer daysUntil;
    
    /**
     * 非数据库字段：下次纪念日日期
     */
    @TableField(exist = false)
    private LocalDate nextAnniversaryDate;
}
