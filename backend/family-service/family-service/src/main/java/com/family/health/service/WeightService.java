package com.family.health.service;

import com.family.family.entity.WeightRecord;

import java.util.List;

/**
 * 体重记录服务接口
 */
public interface WeightService {
    
    /**
     * 保存体重记录
     */
    WeightRecord saveRecord(WeightRecord record);
    
    /**
     * 获取用户体重历史
     */
    List<WeightRecord> getHistory(Long userId);
    
    /**
     * 获取最新体重记录
     */
    WeightRecord getLatest(Long userId);
}
