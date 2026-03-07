package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.UserPoints;
import com.family.family.mapper.UserPointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏模块控制器
 * 处理积分、成就等游戏相关功能
 */
@RestController
@RequestMapping("/api/game")
@SaCheckLogin
public class GameController {

    @Autowired
    private UserPointsMapper userPointsMapper;

    /**
     * 获取当前用户积分
     * GET /api/game/points
     */
    @GetMapping("/points")
    public Map<String, Object> getUserPoints() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 查询用户积分
            UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                    .eq(UserPoints::getUserId, userId)
            );
            
            if (userPoints == null) {
                // 如果没有积分记录，返回默认值
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", Map.of(
                    "points", 0,
                    "totalPoints", 0,
                    "availablePoints", 0,
                    "spentPoints", 0
                ));
                return result;
            }
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "points", userPoints.getAvailablePoints() != null ? userPoints.getAvailablePoints() : 0,
                "totalPoints", userPoints.getTotalPoints() != null ? userPoints.getTotalPoints() : 0,
                "availablePoints", userPoints.getAvailablePoints() != null ? userPoints.getAvailablePoints() : 0,
                "spentPoints", userPoints.getSpentPoints() != null ? userPoints.getSpentPoints() : 0
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取积分失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取积分历史
     * GET /api/game/points/history
     */
    @GetMapping("/points/history")
    public Map<String, Object> getPointsHistory() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // TODO: 实现积分历史查询
            // 暂时返回空列表
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", new ArrayList<>());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取积分历史失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取成就列表
     * GET /api/game/achievements
     */
    @GetMapping("/achievements")
    public Map<String, Object> getAchievements() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // TODO: 实现成就系统
            // 暂时返回空列表
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", new ArrayList<>());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取成就失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取排行榜
     * GET /api/game/rankings/{familyId}
     */
    @GetMapping("/rankings/{familyId}")
    public Map<String, Object> getRankings(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // TODO: 实现排行榜查询
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", new ArrayList<>());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取排行榜失败: " + e.getMessage());
        }
        return result;
    }
}
