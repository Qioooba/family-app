package com.family.health.vo;

import com.family.health.entity.WaterRecord;
import lombok.Data;

import java.util.List;

/**
 * 今日喝水统计VO
 */
@Data
public class WaterTodayVO {
    
    private Long userId;
    private String date;
    private Integer todayAmount;  // 今日已喝量(ml)
    private Integer targetAmount; // 目标量(ml)
    private Integer percent;      // 完成百分比
    private String status;        // completed/half/ongoing
    private String message;       // 提示信息
    private List<WaterRecord> records; // 今日记录列表
}