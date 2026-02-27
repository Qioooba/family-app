package com.family.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI聊天控制器
 * 返回模拟回复
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/ai")
public class AiController {
    
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String userName = request.getOrDefault("userName", "用户");
        
        // 简单的模拟回复逻辑
        String reply = generateReply(message, userName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("reply", reply);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(response);
    }
    
    private String generateReply(String message, String userName) {
        if (message == null || message.trim().isEmpty()) {
            return "你好！有什么我可以帮助你的吗？";
        }
        
        String lowerMsg = message.toLowerCase();
        
        // 问候
        if (lowerMsg.contains("你好") || lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return "你好！" + userName + "！有什么我可以帮你的吗？";
        }
        
        // 天气相关
        if (lowerMsg.contains("天气")) {
            return "今天天气很不错呢！记得带好心情出门～";
        }
        
        // 做饭/菜谱相关
        if (lowerMsg.contains("做饭") || lowerMsg.contains("菜谱") || lowerMsg.contains("吃什么")) {
            return "今天想吃什么呢？可以告诉我你有什么食材，我来帮你推荐菜谱！";
        }
        
        // 提醒相关
        if (lowerMsg.contains("提醒") || lowerMsg.contains("记")) {
            return "好的，你需要我帮你记录什么呢？可以设置纪念日或提醒～";
        }
        
        // 投票相关
        if (lowerMsg.contains("投票")) {
            return "发起投票是个好主意！可以让家人一起决定事情，比如周末去哪玩、吃什么等～";
        }
        
        // 默认回复
        List<String> defaultReplies = Arrays.asList(
            "我明白了！还有什么需要帮忙的吗？",
            "好的，我理解了。还有什么想问的吗？",
            "听起来不错！还有什么我可以帮你的？",
            "我知道了！有什么其他需要吗？"
        );
        
        Random random = new Random();
        return defaultReplies.get(random.nextInt(defaultReplies.size()));
    }
}
