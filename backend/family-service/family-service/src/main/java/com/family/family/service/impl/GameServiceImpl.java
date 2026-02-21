package com.family.family.service.impl;

import com.family.family.entity.GameWheel;
import com.family.family.entity.PointsTransaction;
import com.family.family.mapper.GameWheelMapper;
import com.family.family.service.GameService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏服务实现
 */
@Service
public class GameServiceImpl implements GameService {
    
    private final GameWheelMapper gameWheelMapper;
    
    public GameServiceImpl(GameWheelMapper gameWheelMapper) {
        this.gameWheelMapper = gameWheelMapper;
    }
    
    @Override
    public Long createWheel(Long userId, Object request) {
        GameWheel wheel = new GameWheel();
        gameWheelMapper.insert(wheel);
        return wheel.getId();
    }
    
    @Override
    public List<GameWheel> getWheels(Long familyId) {
        return gameWheelMapper.selectList(null);
    }
    
    @Override
    public String spinWheel(Long wheelId, Long userId) {
        return "谢谢参与";
    }
    
    @Override
    public Map<String, Object> getUserPoints(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("points", 0);
        return result;
    }
    
    @Override
    public List<PointsTransaction> getPointsHistory(Long userId) {
        return new ArrayList<>();
    }
    
    @Override
    public List<Map<String, Object>> getRankings(Long familyId, String type) {
        return new ArrayList<>();
    }
    
    @Override
    public List<Map<String, Object>> getAchievements(Long userId) {
        return new ArrayList<>();
    }
    
    @Override
    public Boolean addPoints(Long userId, Integer points, String reason) {
        return true;
    }
}
