package com.family.family.service;

import java.util.Map;

/**
 * 统计服务
 */
public interface StatsService {
    
    Map<String, Object> getFamilyStats(Long familyId);
    
    Map<String, Object> getUserStats(Long userId);
}
