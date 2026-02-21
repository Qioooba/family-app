package com.family.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 喝水记录实体
 */
@Data
@TableName("water_record")
public class WaterRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer amount; // 喝水量(ml)
    private LocalDate recordDate; // 记录日期
    private LocalTime recordTime; // 记录时间
    private LocalDateTime createTime;
}