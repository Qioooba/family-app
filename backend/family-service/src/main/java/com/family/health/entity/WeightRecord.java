package com.family.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重记录实体
 */
@Data
@TableName("weight_record")
public class WeightRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private BigDecimal weight;
    
    private LocalDate recordDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
