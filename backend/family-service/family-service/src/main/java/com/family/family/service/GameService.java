package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.controller.GameController;
import com.family.family.entity.GameWheel;
import com.family.family.entity.PointsTransaction;
import com.family.family.entity.UserPoints;
import com.family.family.mapper.GameWheelMapper;
import com.family.family.mapper.PointsTransactionMapper;
import com.family.family.mapper.UserPointsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 家庭游戏服务
 */
@Service
public class GameService {
    
    private final GameWheelMapper gameWheelMapper;
    private final UserPointsMapper userPointsMapper;
    private final PointsTransactionMapper pointsTransactionMapper;
    
    public GameService(GameWheelMapper gameWheelMapper, UserPointsMapper userPointsMapper, PointsTransactionMapper pointsTransactionMapper) {
        this.gameWheelMapper = gameWheelMapper;
        this.userPointsMapper = userPointsMapper;
        this.pointsTransactionMapper = pointsTransactionMapper;
    }
    
    public Long createWheel(Long userId, GameController.CreateWheelRequest request) {
        GameWheel wheel = new GameWheel();
        wheel.setFamilyId(request.getFamilyId());
        wheel.setName(request.getName());
        wheel.setItems(request.getItems());
        wheel.setCreatorId(userId);
        wheel.setUseCount(0);
        wheel.setCreateTime(LocalDateTime.now());
        gameWheelMapper.insert(wheel);
        return wheel.getId();
    }
    
    public List<GameWheel> getWheels(Long familyId) {
        LambdaQueryWrapper<GameWheel> wrapper = new LambdaQueryWrapper<GameWheel>()
                .eq(GameWheel::getFamilyId, familyId)
                .orderByDesc(GameWheel::getCreateTime);
        return gameWheelMapper.selectList(wrapper);
    }
    
    @Transactional
    public String spinWheel(Long wheelId, Long userId) {
        GameWheel wheel = gameWheelMapper.selectById(wheelId);
        if (wheel == null) {
            return "转盘不存在";
        }
        
        String itemsJson = wheel.getItems();
        String[] items = itemsJson.replace("[", "").replace("]", "").replace("\"", "").split(",");
        
        Random random = new Random();
        String result = items[random.nextInt(items.length)].trim();
        
        wheel.setUseCount(wheel.getUseCount() + 1);
        gameWheelMapper.updateById(wheel);
        
        return result;
    }
    
    public Map<String, Object> getUserPoints(Long userId) {
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId);
        UserPoints points = userPointsMapper.selectOne(wrapper);
        
        if (points == null) {
            points = new UserPoints();
            points.setUserId(userId);
            points.setTotalPoints(0);
            points.setAvailablePoints(0);
            points.setSpentPoints(0);
            points.setUpdateTime(LocalDateTime.now());
            userPointsMapper.insert(points);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", points.getTotalPoints());
        result.put("available", points.getAvailablePoints());
        result.put("spent", points.getSpentPoints());
        return result;
    }
    
    public List<PointsTransaction> getPointsHistory(Long userId) {
        LambdaQueryWrapper<PointsTransaction> wrapper = new LambdaQueryWrapper<PointsTransaction>()
                .eq(PointsTransaction::getUserId, userId)
                .orderByDesc(PointsTransaction::getCreateTime)
                .last("LIMIT 50");
        return pointsTransactionMapper.selectList(wrapper);
    }
    
    @Transactional
    public Boolean addPoints(Long userId, Integer points, String reason) {
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId);
        UserPoints userPoints = userPointsMapper.selectOne(wrapper);
        
        if (userPoints == null) {
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setAvailablePoints(0);
            userPoints.setSpentPoints(0);
        }
        
        userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
        userPoints.setUpdateTime(LocalDateTime.now());
        
        if (userPoints.getId() == null) {
            userPointsMapper.insert(userPoints);
        } else {
            userPointsMapper.updateById(userPoints);
        }
        
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(userId);
        transaction.setType("earn");
        transaction.setPoints(points);
        transaction.setBalance(userPoints.getAvailablePoints());
        transaction.setReason(reason);
        transaction.setCreateTime(LocalDateTime.now());
        pointsTransactionMapper.insert(transaction);
        
        return true;
    }
    
    public List<Map<String, Object>> getRankings(Long familyId, String type) {
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "妈妈");
        user1.put("score", 2580);
        rankings.add(user1);
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "爸爸");
        user2.put("score", 1920);
        rankings.add(user2);
        
        Map<String, Object> user3 = new HashMap<>();
        user3.put("name", "宝贝");
        user3.put("score", 1250);
        rankings.add(user3);
        
        return rankings;
    }
    
    public List<Map<String, Object>> getAchievements(Long userId) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        Map<String, Object> badge1 = new HashMap<>();
        badge1.put("name", "任务达人");
        badge1.put("icon", "🏆");
        badge1.put("unlocked", true);
        achievements.add(badge1);
        
        Map<String, Object> badge2 = new HashMap<>();
        badge2.put("name", "家务能手");
        badge2.put("icon", "🌟");
        badge2.put("unlocked", true);
        achievements.add(badge2);
        
        Map<String, Object> badge3 = new HashMap<>();
        badge3.put("name", "美食家");
        badge3.put("icon", "🍳");
        badge3.put("unlocked", false);
        achievements.add(badge3);
        
        return achievements;
    }
}
