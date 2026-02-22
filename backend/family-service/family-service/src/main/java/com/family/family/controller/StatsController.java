
package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据统计控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/stats")
public class StatsController {
    
    private final StatsService statsService;
    
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }
    
    @GetMapping("/personal")
    public Result<Map<String, Object>> getPersonalStats(
            @RequestParam String type,
            @RequestParam(required = false) String date) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(statsService.getPersonalStats(userId, type, date));
    }
    
    @GetMapping("/family/{familyId}")
    public Result<Map<String, Object>> getFamilyStats(
            @PathVariable Long familyId,
            @RequestParam String type) {
        return Result.success(statsService.getFamilyStats(familyId, type));
    }
    
    @GetMapping("/tasks")
    public Result<Map<String, Object>> getTaskStats(
            @RequestParam Long familyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statsService.getTaskStats(familyId, startDate, endDate));
    }
    
    @GetMapping("/diet")
    public Result<Map<String, Object>> getDietStats(
            @RequestParam String type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(statsService.getDietStats(userId, type));
    }
    
    @GetMapping("/yearly")
    public Result<Map<String, Object>> getYearlyStats(
            @RequestParam Long familyId,
            @RequestParam int year) {
        return Result.success(statsService.getYearlyStats(familyId, year));
    }
    
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayOverview(
            @RequestParam Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(statsService.getTodayOverview(userId, familyId));
    }
}
