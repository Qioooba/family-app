package com.family.family.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 健康控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    // ========== 饮水记录 ==========
    
    @GetMapping("/water/today")
    public Map<String, Object> getTodayWater(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        
        // Return a realistic amount based on user
        int todayAmount = 500; // Default mock value
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("date", java.time.LocalDate.now().toString());
        data.put("todayAmount", todayAmount);
        data.put("targetAmount", 2000);
        data.put("percent", (todayAmount * 100) / 2000);
        data.put("status", todayAmount >= 2000 ? "completed" : "ongoing");
        data.put("message", todayAmount >= 2000 ? "今日目标已完成！" : "记得多喝水哦~");
        data.put("records", new ArrayList<>());
        result.put("data", data);
        return result;
    }
    
    @GetMapping("/water/target")
    public Map<String, Object> getWaterTarget(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("defaultTarget", 2000);
        data.put("targetAmount", 2000);
        data.put("userId", userId);
        result.put("data", data);
        return result;
    }
    
    @PostMapping("/water")
    public Map<String, Object> addWaterRecord(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @DeleteMapping("/water/{id}")
    public Map<String, Object> deleteWaterRecord(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return result;
    }
    
    @GetMapping("/water/history")
    public Map<String, Object> getWaterHistory(@RequestParam Long userId, @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }
}
