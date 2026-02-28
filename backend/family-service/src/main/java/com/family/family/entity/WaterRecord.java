package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 喝水记录实体
 */
@TableName("water_record")
public class WaterRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer amount;
    private LocalDate recordDate;
    private LocalDateTime createTime;
    
    // 使用 java.sql.Time 存储时间，与数据库 time 类型匹配
    private java.sql.Time recordTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public java.sql.Time getRecordTime() {
        return recordTime;
    }
    
    public void setRecordTime(java.sql.Time recordTime) {
        this.recordTime = recordTime;
    }
    
    /**
     * 获取 LocalTime 类型的 recordTime
     */
    public LocalTime getRecordTimeAsLocalTime() {
        if (recordTime == null) {
            return null;
        }
        return recordTime.toLocalTime();
    }
    
    /**
     * 设置 LocalTime 类型的 recordTime
     */
    public void setRecordTimeFromLocalTime(LocalTime time) {
        if (time == null) {
            this.recordTime = null;
        } else {
            this.recordTime = java.sql.Time.valueOf(time);
        }
    }
}
