package com.family.health.service.impl;

import com.family.family.entity.UserWaterGoal;
import com.family.health.mapper.UserWaterGoalMapper;
import com.family.health.service.UserWaterGoalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户饮水目标服务实现
 */
@Service
public class UserWaterGoalServiceImpl implements UserWaterGoalService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserWaterGoalServiceImpl.class);
    
    // 默认每日目标饮水量 2000ml
    private static final int DEFAULT_TARGET = 2000;
    
    private final UserWaterGoalMapper userWaterGoalMapper;
    
    public UserWaterGoalServiceImpl(UserWaterGoalMapper userWaterGoalMapper) {
        this.userWaterGoalMapper = userWaterGoalMapper;
    }
    
    @Override
    public Integer getUserTarget(Long userId) {
        if (userId == null) {
            return DEFAULT_TARGET;
        }
        try {
            UserWaterGoal goal = userWaterGoalMapper.selectByUserId(userId);
            if (goal == null || goal.getTargetAmount() == null) {
                return DEFAULT_TARGET;
            }
            return goal.getTargetAmount();
        } catch (Exception e) {
            // 表不存在或其他数据库错误时返回默认值
            logger.warn("获取用户饮水目标失败，使用默认值 {}: {}", DEFAULT_TARGET, e.getMessage());
            return DEFAULT_TARGET;
        }
    }
    
    @Override
    public boolean setUserTarget(Long userId, Integer targetAmount) {
        if (userId == null || targetAmount == null || targetAmount < 500 || targetAmount > 5000) {
            return false;
        }
        
        try {
            UserWaterGoal existingGoal = userWaterGoalMapper.selectByUserId(userId);
            LocalDateTime now = LocalDateTime.now();
            
            if (existingGoal == null) {
                // 创建新记录
                UserWaterGoal goal = new UserWaterGoal();
                goal.setUserId(userId);
                goal.setTargetAmount(targetAmount);
                goal.setCreateTime(now);
                goal.setUpdateTime(now);
                return userWaterGoalMapper.insert(goal) > 0;
            } else {
                // 更新现有记录
                existingGoal.setTargetAmount(targetAmount);
                existingGoal.setUpdateTime(now);
                return userWaterGoalMapper.updateById(existingGoal) > 0;
            }
        } catch (Exception e) {
            logger.error("设置用户饮水目标失败: {}", e.getMessage());
            return false;
        }
    }
}