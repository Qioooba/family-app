package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计缓存
 */
@Data
@TableName("stats_cache")
public class StatsCache {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 统计类型
     */
    private String statType;
    
    /**
     * 目标ID（用户ID或家庭ID）
     */
    private Long targetId;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 统计结果JSON
     */
    private String data;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
