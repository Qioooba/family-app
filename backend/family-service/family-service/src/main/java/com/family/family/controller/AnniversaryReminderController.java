package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 纪念日多级提醒控制器
 */
@RestController
@RequestMapping("/api/anniversary")
@SaCheckLogin
@RequiredArgsConstructor
public class AnniversaryReminderController {
    
    /**
     * 设置多级提醒
     * POST /api/anniversary/reminder
     */
    @PostMapping("/reminder")
    public Result<Boolean> setReminder(@RequestBody ReminderRequest request) {
        // 实际实现需要创建提醒设置表
        return Result.success(true);
    }
    
    /**
     * 生成贺卡
     * POST /api/anniversary/card
     */
    @PostMapping("/card")
    public Result<Map<String, Object>> generateCard(@RequestBody CardRequest request) {
        Map<String, Object> card = new HashMap<>();
        card.put("cardId", System.currentTimeMillis());
        card.put("title", request.getTitle());
        card.put("message", request.getMessage());
        card.put("template", request.getTemplate());
        card.put("generatedAt", java.time.LocalDateTime.now().toString());
        card.put("imageUrl", "https://example.com/cards/" + System.currentTimeMillis() + ".png");
        return Result.success(card);
    }
    
    /**
     * 获取贺卡模板列表
     * GET /api/anniversary/card-templates
     */
    @GetMapping("/card-templates")
    public Result<List<Map<String, String>>> getCardTemplates() {
        return Result.success(List.of(
            Map.of("id", "1", "name", "经典模板", "preview", "classic.png"),
            Map.of("id", "2", "name", "浪漫模板", "preview", "romantic.png"),
            Map.of("id", "3", "name", "简约模板", "preview", "minimal.png"),
            Map.of("id", "4", "name", "童趣模板", "preview", "cartoon.png")
        ));
    }
    
    /**
     * 提醒请求
     */
    public static class ReminderRequest {
        private Long anniversaryId;
        private List<Integer> remindDays; // 提前提醒天数 [7, 3, 1] 表示提前7天、3天、1天提醒
        private String remindTime; // 提醒时间，如 "09:00"
        private Boolean isRepeatYearly;
        
        public Long getAnniversaryId() { return anniversaryId; }
        public void setAnniversaryId(Long anniversaryId) { this.anniversaryId = anniversaryId; }
        
        public List<Integer> getRemindDays() { return remindDays; }
        public void setRemindDays(List<Integer> remindDays) { this.remindDays = remindDays; }
        
        public String getRemindTime() { return remindTime; }
        public void setRemindTime(String remindTime) { this.remindTime = remindTime; }
        
        public Boolean getIsRepeatYearly() { return isRepeatYearly; }
        public void setIsRepeatYearly(Boolean isRepeatYearly) { this.isRepeatYearly = isRepeatYearly; }
    }
    
    /**
     * 贺卡请求
     */
    public static class CardRequest {
        private String title;
        private String message;
        private String template;
        private String recipientName;
        private String senderName;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getTemplate() { return template; }
        public void setTemplate(String template) { this.template = template; }
        
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
    }
}
