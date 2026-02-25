package com.family.health.service;

import com.family.family.entity.UserWeight;

/**
 * 用户饮水目标服务接口
 */
public interface UserWaterGoalService {
    
    /**
     * 获取用户的饮水目标
     * @param userId 用户ID
     * @return 目标饮水量（毫升），未设置返回默认2000
     */
    Integer getUserTarget(Long userId);
    
    /**
     * 设置用户的饮水目标
     * @param userId 用户ID
     * @param targetAmount 目标饮水量（毫升）
     * @return 是否设置成功
     */
    boolean setUserTarget(Long userId, Integer targetAmount);
}