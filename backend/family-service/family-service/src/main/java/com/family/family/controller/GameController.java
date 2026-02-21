package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.GameWheel;
import com.family.family.entity.PointsTransaction;
import com.family.family.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家庭游戏控制器
 */
@RestController
@RequestMapping("/api/game")
public class GameController {
    
    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    @PostMapping("/wheel/create")
    public Result<Long> createWheel(@RequestBody CreateWheelRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.createWheel(userId, request));
    }
    
    @GetMapping("/wheel/{familyId}")
    public Result<List<GameWheel>> getWheels(@PathVariable Long familyId) {
        return Result.success(gameService.getWheels(familyId));
    }
    
    @PostMapping("/wheel/{wheelId}/spin")
    public Result<String> spinWheel(@PathVariable Long wheelId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.spinWheel(wheelId, userId));
    }
    
    @GetMapping("/points")
    public Result<Map<String, Object>> getUserPoints() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getUserPoints(userId));
    }
    
    @GetMapping("/points/history")
    public Result<List<PointsTransaction>> getPointsHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getPointsHistory(userId));
    }
    
    @GetMapping("/rankings/{familyId}")
    public Result<List<Map<String, Object>>> getRankings(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "points") String type) {
        return Result.success(gameService.getRankings(familyId, type));
    }
    
    @GetMapping("/achievements")
    public Result<List<Map<String, Object>>> getAchievements() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getAchievements(userId));
    }
    
    @PostMapping("/points/add")
    public Result<Boolean> addPoints(@RequestBody AddPointsRequest request) {
        return Result.success(gameService.addPoints(request.getUserId(), request.getPoints(), request.getReason()));
    }
    
    public static class CreateWheelRequest {
        private Long familyId;
        private String name;
        private String items;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getItems() {
            return items;
        }
        
        public void setItems(String items) {
            this.items = items;
        }
    }
    
    public static class AddPointsRequest {
        private Long userId;
        private Integer points;
        private String reason;
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Integer getPoints() {
            return points;
        }
        
        public void setPoints(Integer points) {
            this.points = points;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
