package com.family.family.service.impl;

import com.family.family.service.GameService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏服务实现
 */
@Service
public class GameServiceImpl implements GameService {
    
    @Override
    public Map<String, Object> spinWheel(Long wheelId) {
        Map<String, Object> result = new HashMap<>();
        result.put("prize", "谢谢参与");
        return result;
    }
    
    @Override
    public Map<String, Object> getPoints(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("points", 0);
        return result;
    }
    
    @Override
    public void addPoints(Long userId, int points, String reason) {
    }
}
