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
}
