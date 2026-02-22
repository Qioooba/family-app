package com.family.ai.controller;

import com.family.ai.entity.AiInventory;
import com.family.ai.entity.AiShoppingList;
import com.family.ai.entity.AiSubstitute;
import com.family.ai.service.AiChatService;
import com.family.ai.service.AiDailyReportService;
import com.family.ai.service.AiShoppingService;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI助手控制器
 */
@RestController
@RequestMapping("/api/ai")
@SaCheckLogin
public class AiController {
    
    private final AiChatService aiChatService;
    private final AiDailyReportService aiDailyReportService;
    private final AiShoppingService aiShoppingService;
    
    public AiController(AiChatService aiChatService, 
                       AiDailyReportService aiDailyReportService,
                       AiShoppingService aiShoppingService) {
        this.aiChatService = aiChatService;
        this.aiDailyReportService = aiDailyReportService;
        this.aiShoppingService = aiShoppingService;
    }
    
    // ========== AI对话 ==========
    
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
    
    @GetMapping("/chat/history")
    public Result<Object> getChatHistory(@RequestParam String sessionId) {
        return Result.success(aiChatService.getChatHistory(sessionId));
    }
    
    @PostMapping("/chat/session")
    public Result<Map<String, String>> createSession() {
        Map<String, String> result = new HashMap<>();
        result.put("sessionId", aiChatService.generateSessionId());
        return Result.success(result);
    }
    
    // ========== 日报功能 ==========
    
    @GetMapping("/daily-report/morning")
    public Result<String> getMorningReport(@RequestParam String userName) {
        Long userId = StpUtil.getLoginIdAsLong();
        String report = aiDailyReportService.generateMorningReport(userId, userName, LocalDate.now());
        return Result.success(report);
    }
    
    @GetMapping("/daily-report/evening")
    public Result<String> getEveningSummary(@RequestParam String userName) {
        Long userId = StpUtil.getLoginIdAsLong();
        String summary = aiDailyReportService.generateEveningSummary(userId, userName, LocalDate.now());
        return Result.success(summary);
    }
    
    @PostMapping("/recipe/recommend")
    public Result<String> recommendRecipe(@RequestBody RecipeRecommendRequest request) {
        String recommendation = aiDailyReportService.recommendRecipe(
                request.getIngredients(), 
                request.getPreference()
        );
        return Result.success(recommendation);
    }
    
    @PostMapping("/nutrition/analyze")
    public Result<String> analyzeNutrition(@RequestBody NutritionAnalyzeRequest request) {
        String analysis = aiDailyReportService.analyzeNutrition(request.getTodayDiet());
        return Result.success(analysis);
    }
    
    // ========== 购物助手 ==========
    
    @PostMapping("/shopping/generate")
    public Result<AiShoppingList> generateShoppingList(@RequestBody GenerateShoppingRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(aiShoppingService.generateShoppingList(
                request.getFamilyId(), userId, request.getOccasion(), request.getRequirements()));
    }
    
    @PostMapping("/shopping/list")
    public Result<AiShoppingList> saveShoppingList(@RequestBody AiShoppingList list) {
        return Result.success(aiShoppingService.saveShoppingList(list));
    }
    
    @DeleteMapping("/shopping/list/{id}")
    public Result<Void> deleteShoppingList(@PathVariable Long id) {
        aiShoppingService.deleteShoppingList(id);
        return Result.success();
    }
    
    @GetMapping("/shopping/list/{id}")
    public Result<AiShoppingList> getShoppingListById(@PathVariable Long id) {
        return Result.success(aiShoppingService.getShoppingListById(id));
    }
    
    @GetMapping("/shopping/lists/{familyId}")
    public Result<List<AiShoppingList>> getFamilyShoppingLists(@PathVariable Long familyId) {
        return Result.success(aiShoppingService.getFamilyShoppingLists(familyId));
    }
    
    @PostMapping("/shopping/list/{id}/check")
    public Result<Void> checkShoppingItem(@PathVariable Long id, 
                                             @RequestParam String itemName,
                                             @RequestParam Integer checked) {
        aiShoppingService.checkShoppingItem(id, itemName, checked);
        return Result.success();
    }
    
    // ========== 库存管理 ==========
    
    @PostMapping("/inventory")
    public Result<AiInventory> addInventory(@RequestBody AiInventory inventory) {
        Long userId = StpUtil.getLoginIdAsLong();
        inventory.setUserId(userId);
        return Result.success(aiShoppingService.addInventory(inventory));
    }
    
    @PutMapping("/inventory/{id}")
    public Result<Void> updateInventory(@PathVariable Long id, @RequestParam java.math.BigDecimal quantity) {
        aiShoppingService.updateInventory(id, quantity);
        return Result.success();
    }
    
    @DeleteMapping("/inventory/{id}")
    public Result<Void> deleteInventory(@PathVariable Long id) {
        aiShoppingService.deleteInventory(id);
        return Result.success();
    }
    
    @GetMapping("/inventory/{id}")
    public Result<AiInventory> getInventoryById(@PathVariable Long id) {
        return Result.success(aiShoppingService.getInventoryById(id));
    }
    
    @GetMapping("/inventories/{familyId}")
    public Result<List<AiInventory>> getFamilyInventory(@PathVariable Long familyId) {
        return Result.success(aiShoppingService.getFamilyInventory(familyId));
    }
    
    @GetMapping("/inventories/low-stock/{familyId}")
    public Result<List<AiInventory>> getLowStockItems(@PathVariable Long familyId) {
        return Result.success(aiShoppingService.getLowStockItems(familyId));
    }
    
    @GetMapping("/inventories/category/{familyId}")
    public Result<List<AiInventory>> getInventoryByCategory(@PathVariable Long familyId,
                                                               @RequestParam String category) {
        return Result.success(aiShoppingService.getInventoryByCategory(familyId, category));
    }
    
    // ========== 食材替代 ==========
    
    @GetMapping("/substitute/{ingredient}")
    public Result<List<AiSubstitute>> getSubstitutes(@PathVariable String ingredient) {
        return Result.success(aiShoppingService.getSubstitutes(ingredient));
    }
    
    @PostMapping("/substitute/advice")
    public Result<String> getSubstituteAdvice(@RequestBody SubstituteRequest request) {
        return Result.success(aiShoppingService.getSubstituteAdvice(request.getIngredient(), request.getContext()));
    }
    
    // ========== 采购优化 ==========
    
    @PostMapping("/shopping/optimize")
    public Result<Map<String, Object>> optimizePurchase(@RequestBody OptimizeRequest request) {
        return Result.success(aiShoppingService.optimizePurchase(request.getFamilyId(), request.getItems()));
    }
    
    @GetMapping("/shopping/analysis/{familyId}")
    public Result<Map<String, Object>> analyzeShoppingHistory(@PathVariable Long familyId,
                                                               @RequestParam(defaultValue = "3") Integer months) {
        return Result.success(aiShoppingService.analyzeShoppingHistory(familyId, months));
    }
    
    // ========== 请求类 ==========
    
    public static class ChatRequest {
        private String message;
        private Long familyId;
        private String sessionId;
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class RecipeRecommendRequest {
        private String ingredients;
        private String preference;
        
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        public String getPreference() { return preference; }
        public void setPreference(String preference) { this.preference = preference; }
    }
    
    public static class NutritionAnalyzeRequest {
        private String todayDiet;
        
        public String getTodayDiet() { return todayDiet; }
        public void setTodayDiet(String todayDiet) { this.todayDiet = todayDiet; }
    }
    
    public static class GenerateShoppingRequest {
        private Long familyId;
        private String occasion;
        private String requirements;
        
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
        public String getOccasion() { return occasion; }
        public void setOccasion(String occasion) { this.occasion = occasion; }
        public String getRequirements() { return requirements; }
        public void setRequirements(String requirements) { this.requirements = requirements; }
    }
    
    public static class SubstituteRequest {
        private String ingredient;
        private String context;
        
        public String getIngredient() { return ingredient; }
        public void setIngredient(String ingredient) { this.ingredient = ingredient; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
    
    public static class OptimizeRequest {
        private Long familyId;
        private List<String> items;
        
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }
}
