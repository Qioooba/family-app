package com.family.family.service;

import java.util.Map;

/**
 * 统计服务
 */
public interface StatsService {
    
    Map<String, Object> getFamilyStats(Long familyId, String type);
    
    Map<String, Object> getUserStats(Long userId);
    
    Map<String, Object> getPersonalStats(Long userId, String type, String date);
    
    Map<String, Object> getTaskStats(Long familyId, String startDate, String endDate);
    
    Map<String, Object> getDietStats(Long userId, String type);
    
    Map<String, Object> getYearlyStats(Long familyId, int year);
    
    Map<String, Object> getTodayOverview(Long userId, Long familyId);
    
    /**
     * 获取家庭本月统计
     * @param familyId 家庭ID
     * @return 本月统计数据
     */
    Map<String, Object> getFamilyMonthlyStats(Long familyId);
}
