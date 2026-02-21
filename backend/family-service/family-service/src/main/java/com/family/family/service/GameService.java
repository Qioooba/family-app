package com.family.family.service;

import com.family.family.entity.GameWheel;
import com.family.family.entity.PointsTransaction;

import java.util.List;
import java.util.Map;

/**
 * 游戏服务
 */
public interface GameService {
    
    Long createWheel(Long userId, Object request);
    
    List<GameWheel> getWheels(Long familyId);
    
    String spinWheel(Long wheelId, Long userId);
    
    Map<String, Object> getUserPoints(Long userId);
    
    List<PointsTransaction> getPointsHistory(Long userId);
    
    List<Map<String, Object>> getRankings(Long familyId, String type);
    
    List<Map<String, Object>> getAchievements(Long userId);
    
    Boolean addPoints(Long userId, Integer points, String reason);
}
