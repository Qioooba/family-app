package com.family.health.controller;

import com.family.common.core.Result;
import com.family.health.service.DietService;
import com.family.health.vo.DailyNutritionVO;
import com.family.health.vo.WeeklyNutritionVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 饮食控制器
 * 提供饮食记录和营养分析API
 */
@RestController
@RequestMapping("/api/diet")
public class DietController {
    
    private final DietService dietService;
    
    public DietController(DietService dietService) {
        this.dietService = dietService;
    }
    
    /**
     * 获取每日营养分析
     * GET /api/diet/nutrition/daily
     */
    @GetMapping("/nutrition/daily")
    public Result<DailyNutritionVO> getDailyNutrition(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(dietService.getDailyNutrition(userId, date));
    }
    
    /**
     * 获取周营养报告
     * GET /api/diet/nutrition/weekly
     */
    @GetMapping("/nutrition/weekly")
    public Result<WeeklyNutritionVO> getWeeklyNutrition(@RequestParam Long userId) {
        return Result.success(dietService.getWeeklyNutrition(userId));
    }
}
