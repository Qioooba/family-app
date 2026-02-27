package com.family.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@SaCheckLogin
public class AiController {

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String message = (String) params.get("message");
        
        result.put("code", 200);
        result.put("message", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("reply", "你好！用户！有什么我可以帮你的吗？");
        data.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return result;
    }
    
    @GetMapping("/capabilities")
    public Map<String, Object> capabilities() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("chat", true);
        data.put("voice", true);
        data.put("nutritionist", true);
        data.put("shoppingAssistant", true);
        result.put("data", data);
        
        return result;
    }
}
