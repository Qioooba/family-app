package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.family.entity.WaterRecord;
import com.family.family.service.StatsService;
import com.family.family.service.WaterRecordService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 健康控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final StatsService statsService;
    private final WaterRecordService waterRecordService;
    
    public HealthController(StatsService statsService, WaterRecordService waterRecordService) {
        this.statsService = statsService;
        this.waterRecordService = waterRecordService;
    }

    // ========== 饮水记录 ==========
    
    @GetMapping("/water/today")
    public Map<String, Object> getTodayWater(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> data = waterRecordService.getTodayWaterInfo(userId);
            
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
    public Map<String, Object> getWaterTarget(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        
        // 从数据库获取用户保存的目标，如果没有则返回默认值
        Integer targetAmount = waterRecordService.getUserWaterTarget(userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("defaultTarget", 2000);
        data.put("targetAmount", targetAmount);
        data.put("userId", userId);
        result.put("data", data);
        return result;
    }
    
    @PostMapping("/water/target")
    public Map<String, Object> setWaterTarget(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            Integer targetAmount = Integer.valueOf(data.get("targetAmount").toString());
            
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
            result.put("data", data);
        }
        
        return result;
    }
    
    @PostMapping("/water/record")
    public Map<String, Object> addWaterRecord(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从 token 获取当前用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            Integer amount = Integer.valueOf(data.get("amount").toString());
            
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
        
        try {
            boolean success = waterRecordService.deleteRecord(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 404);
                result.put("message", "记录不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/water/history")
    public Map<String, Object> getWaterHistory(@RequestParam Long userId, @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            List<WaterRecord> records = waterRecordService.getRecordsByDate(userId, localDate);
            
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
    public Map<String, Object> getDietStatistics(@RequestParam Long userId, @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析日期
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // 获取统计数据
            Map<String, Object> data = statsService.getDailyDietStats(userId, localDate);
            
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
