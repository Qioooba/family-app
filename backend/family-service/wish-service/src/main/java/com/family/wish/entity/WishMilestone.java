package com.family.wish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心愿里程碑实体
 */
@Data
@TableName("wish_milestone")
public class WishMilestone {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long wishId;
    private String title;
    private String description;
    private LocalDate targetDate;
    private Integer isCompleted;
    private Integer sortOrder;
    private LocalDateTime createTime;
}