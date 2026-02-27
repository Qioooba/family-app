package com.family.family.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        // Return default user data
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("username", "15861890687");
        data.put("nickname", "好好");
        result.put("data", data);
        return result;
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", "test-token-12345");
        return result;
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 简单模拟注册
        String phone = (String) params.get("phone");
        String nickname = (String) params.get("nickname");
        
        if (phone == null || phone.isEmpty()) {
            result.put("code", 400);
            result.put("message", "手机号不能为空");
            return result;
        }
        
        // 返回模拟注册成功
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
            "id", 100,
            "phone", phone,
            "nickname", nickname != null ? nickname : "新用户"
        ));
        return result;
    }
}
