package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;

import cn.dev33.satoken.stp.StpUtil;
import com.family.family.dto.request.WaterRecordCreateRequest;
import com.family.family.dto.request.WaterTargetRequest;
import com.family.family.entity.WaterRecord;
import com.family.family.service.StatsService;
import com.family.family.service.WaterRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 健康控制器
 */
@RestController
@RequestMapping("/api/health")
@SaCheckLogin
public class HealthController {
    
    private final StatsService statsService;
    private final WaterRecordService waterRecordService;
    
    public HealthController(StatsService statsService, WaterRecordService waterRecordService) {
        this.statsService = statsService;
        this.waterRecordService = waterRecordService;
    }

    // ========== 饮水记录 ==========
    
    @GetMapping("/water/today")
    public Map<String, Object> getTodayWater(@RequestParam(required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        try {
            Map<String, Object> data = waterRecordService.getTodayWaterInfo(currentUserId);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取今日喝水数据失败: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        
        return result;
    }
    
    @GetMapping("/water/target")
    public Map<String, Object> getWaterTarget(@RequestParam(required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        result.put("code", 200);
        
        // 从数据库获取用户保存的目标，如果没有则返回默认值
        Integer targetAmount = waterRecordService.getUserWaterTarget(currentUserId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("defaultTarget", 2000);
        data.put("targetAmount", targetAmount);
        data.put("userId", currentUserId);
        result.put("data", data);
        return result;
    }
    
    @PostMapping("/water/target")
    public Map<String, Object> setWaterTarget(@Valid @RequestBody WaterTargetRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Integer targetAmount = request.getTargetAmount();
            
            // 保存到数据库
            Integer savedTarget = waterRecordService.saveUserWaterTarget(userId, targetAmount);
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", userId);
            resultData.put("targetAmount", savedTarget);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", resultData);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "保存喝水目标失败: " + e.getMessage());
            result.put("data", Map.of("targetAmount", request.getTargetAmount()));
        }
        
        return result;
    }
    
    @PostMapping("/water/record")
    public Map<String, Object> addWaterRecord(@Valid @RequestBody WaterRecordCreateRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从 token 获取当前用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            Integer amount = request.getAmount();
            
            WaterRecord record = waterRecordService.addRecord(userId, amount);
            
            // 转换为 Map 避免 Jackson 序列化问题
            Map<String, Object> recordData = new HashMap<>();
            recordData.put("id", record.getId());
            recordData.put("userId", record.getUserId());
            recordData.put("amount", record.getAmount());
            recordData.put("recordDate", record.getRecordDate().toString());
            recordData.put("recordTime", record.getRecordTime().toString());
            recordData.put("createTime", record.getCreateTime().toString());
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", recordData);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加喝水记录失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @DeleteMapping("/water/{id}")
    public Map<String, Object> deleteWaterRecord(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        try {
            boolean success = waterRecordService.deleteRecord(currentUserId, id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 403);
                result.put("message", "记录不存在或无权限");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/water/history")
    public Map<String, Object> getWaterHistory(@RequestParam(required = false) Long userId, @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            List<WaterRecord> records = waterRecordService.getRecordsByDate(currentUserId, localDate);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", records);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取历史记录失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return result;
    }
    
    // ========== 饮食统计 ==========
    
    /**
     * 获取某日饮食统计
     * @param userId 用户ID
     * @param date 日期 (YYYY-MM-DD)
     * @return 当日饮食统计数据
     */
    @GetMapping("/diet/statistics")
    public Map<String, Object> getDietStatistics(@RequestParam(required = false) Long userId, @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        try {
            // 解析日期
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // 获取统计数据
            Map<String, Object> data = statsService.getDailyDietStats(currentUserId, localDate);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取饮食统计失败: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        
        return result;
    }
}
