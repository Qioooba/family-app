package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.DietRecord;
import com.family.family.service.DietRecordService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 饮食记录控制器
 */
@RestController
@RequestMapping("/api/health/diet")
@SaCheckLogin
public class DietRecordController {

    private final DietRecordService dietRecordService;

    public DietRecordController(DietRecordService dietRecordService) {
        this.dietRecordService = dietRecordService;
    }

    /**
     * 获取今日饮食记录
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getToday() {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate today = LocalDate.now();
        
        List<DietRecord> records = dietRecordService.getByUserIdAndDate(userId, today);
        
        // 计算总热量
        int totalCalories = records.stream()
                .mapToInt(DietRecord::getCalories)
                .sum();
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", today.toString());
        result.put("records", records);
        result.put("totalCalories", totalCalories);
        result.put("targetCalories", 2000); // 默认目标
        
        return Result.success(result);
    }

    /**
     * 获取指定日期饮食记录
     */
    @GetMapping("/list")
    public Result<List<DietRecord>> list(
            @RequestParam String date,
            @RequestParam(required = false) Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate localDate = LocalDate.parse(date);
        
        List<DietRecord> records = dietRecordService.getByUserIdAndDate(userId, localDate);
        return Result.success(records);
    }

    /**
     * 添加饮食记录
     */
    @PostMapping("/add")
    public Result<DietRecord> add(@RequestBody DietRecord record) {
        Long userId = StpUtil.getLoginIdAsLong();
        record.setUserId(userId);
        record.setRecordDate(LocalDate.now());
        
        dietRecordService.save(record);
        return Result.success(record);
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dietRecordService.removeById(id);
        return Result.success(null);
    }
}
