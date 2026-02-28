package com.family.family.service;

import com.family.family.entity.WaterRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 喝水记录服务
 */
public interface WaterRecordService {
    
    /**
     * 添加喝水记录
     */
    WaterRecord addRecord(Long userId, Integer amount);
    
    /**
     * 获取用户指定日期的喝水记录
     */
    List<WaterRecord> getRecordsByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户指定日期的喝水总量
     */
    Integer getTotalAmountByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户今日喝水信息
     */
    Map<String, Object> getTodayWaterInfo(Long userId);
    
    /**
     * 删除喝水记录
     */
    boolean deleteRecord(Long recordId);
    
    /**
     * 获取用户喝水目标
     * @return 目标值（毫升），默认2000
     */
    Integer getUserWaterTarget(Long userId);
    
    /**
     * 保存用户喝水目标
     * @param userId 用户ID
     * @param targetAmount 目标值（毫升）
     * @return 保存后的目标值
     */
    Integer saveUserWaterTarget(Long userId, Integer targetAmount);
}
