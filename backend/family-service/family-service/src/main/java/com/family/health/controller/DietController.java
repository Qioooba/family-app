
package com.family.health.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.DietRecord;
import com.family.health.service.DietService;
import com.family.health.vo.DailyNutritionVO;
import com.family.health.vo.WeeklyNutritionVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 饮食控制器
 * 提供饮食记录和营养分析API
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/health/diet")
public class DietController {

    private final DietService dietService;

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    /**
     * 添加饮食记录
     * POST /api/health/diet/record
     */
    @PostMapping("/record")
    public Result<DietRecord> addRecord(@RequestBody DietRecord record) {
        // 从token获取用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        record.setUserId(userId);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        return Result.success(dietService.addRecord(record));
    }

    /**
     * 获取某日饮食记录
     * GET /api/health/diet/day
     */
    @GetMapping("/day")
    public Result<List<DietRecord>> getDayRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(dietService.getDayRecords(userId, date));
    }

    /**
     * 获取某日统计
     * GET /api/health/diet/statistics
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDayStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> stats = dietService.getDayStatistics(userId, date);
        // 添加目标值
        stats.put("intake", stats.get("totalCalories"));
        stats.put("target", 2000);
        stats.put("remaining", 2000 - ((Number) stats.get("totalCalories")).intValue());
        return Result.success(stats);
    }

    /**
     * 获取每日营养分析
     * GET /api/health/diet/nutrition/daily
     */
    @GetMapping("/nutrition/daily")
    public Result<DailyNutritionVO> getDailyNutrition(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(dietService.getDailyNutrition(userId, date));
    }

    /**
     * 获取周营养报告
     * GET /api/health/diet/weekly
     */
    @GetMapping("/weekly")
    public Result<WeeklyNutritionVO> getWeeklyNutrition(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }
        return Result.success(dietService.getWeeklyNutrition(userId));
    }

    /**
     * 删除记录
     * DELETE /api/health/diet/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(dietService.removeById(id));
    }
}
