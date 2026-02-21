package com.family.family.service;

import java.util.Map;

/**
 * 游戏服务
 */
public interface GameService {
    
    Map<String, Object> spinWheel(Long wheelId);
    
    Map<String, Object> getPoints(Long userId);
    
    void addPoints(Long userId, int points, String reason);
}
