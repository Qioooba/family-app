package com.family.family.service.impl;

import com.family.family.service.StatsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计服务实现
 */
@Service
public class StatsServiceImpl implements StatsService {
    
    @Override
    public Map<String, Object> getFamilyStats(Long familyId) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getUserStats(Long userId) {
        return new HashMap<>();
    }
}
