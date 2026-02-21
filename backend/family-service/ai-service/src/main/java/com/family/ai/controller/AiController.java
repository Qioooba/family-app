package com.family.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.ai.service.AiChatService;
import com.family.ai.service.AiDailyReportService;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * AI助手控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    private final AiChatService aiChatService;
    private final AiDailyReportService aiDailyReportService;
    
    public AiController(AiChatService aiChatService, AiDailyReportService aiDailyReportService) {
        this.aiChatService = aiChatService;
        this.aiDailyReportService = aiDailyReportService;
    }
    
    /**
     * AI对话
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = aiChatService.generateSessionId();
        }
        
        String reply = aiChatService.chat(userId, request.getFamilyId(), request.getMessage(), sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("sessionId", sessionId);
        
        return Result.success(result);
    }
    
    /**
     * 获取对话历史
     */
    @GetMapping("/chat/history")
    public Result<Object> getChatHistory(@RequestParam String sessionId) {
        return Result.success(aiChatService.getChatHistory(sessionId));
    }
    
    /**
     * 生成新会话
     */
    @PostMapping("/chat/session")
    public Result<Map<String, String>> createSession() {
        Map<String, String> result = new HashMap<>();
        result.put("sessionId", aiChatService.generateSessionId());
        return Result.success(result);
    }
    
    /**
     * 获取早安日报
     */
    @GetMapping("/daily-report/morning")
    public Result<String> getMorningReport(@RequestParam String userName) {
        Long userId = StpUtil.getLoginIdAsLong();
        String report = aiDailyReportService.generateMorningReport(userId, userName, LocalDate.now());
        return Result.success(report);
    }
    
    /**
     * 获取晚安总结
     */
    @GetMapping("/daily-report/evening")
    public Result<String> getEveningSummary(@RequestParam String userName) {
        Long userId = StpUtil.getLoginIdAsLong();
        String summary = aiDailyReportService.generateEveningSummary(userId, userName, LocalDate.now());
        return Result.success(summary);
    }
    
    /**
     * 菜谱推荐
     */
    @PostMapping("/recipe/recommend")
    public Result<String> recommendRecipe(@RequestBody RecipeRecommendRequest request) {
        String recommendation = aiDailyReportService.recommendRecipe(
                request.getIngredients(), 
                request.getPreference()
        );
        return Result.success(recommendation);
    }
    
    /**
     * 营养分析
     */
    @PostMapping("/nutrition/analyze")
    public Result<String> analyzeNutrition(@RequestBody NutritionAnalyzeRequest request) {
        String analysis = aiDailyReportService.analyzeNutrition(request.getTodayDiet());
        return Result.success(analysis);
    }
    
    // ========== 请求类 ==========
    
    public static class ChatRequest {
        private String message;
        private Long familyId;
        private String sessionId;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
    
    public static class RecipeRecommendRequest {
        private String ingredients;
        private String preference;
        
        public String getIngredients() {
            return ingredients;
        }
        
        public void setIngredients(String ingredients) {
            this.ingredients = ingredients;
        }
        
        public String getPreference() {
            return preference;
        }
        
        public void setPreference(String preference) {
            this.preference = preference;
        }
    }
    
    public static class NutritionAnalyzeRequest {
        private String todayDiet;
        
        public String getTodayDiet() {
            return todayDiet;
        }
        
        public void setTodayDiet(String todayDiet) {
            this.todayDiet = todayDiet;
        }
    }
}
