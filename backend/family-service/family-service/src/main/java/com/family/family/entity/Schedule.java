package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班实体
 */
@Data
@TableName("schedule")
public class Schedule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long familyId;
    private Long userId;
    private Long creatorId;
    private String title;
    private String description;
    private LocalDate workDate;
    private String startTime;
    private String endTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
