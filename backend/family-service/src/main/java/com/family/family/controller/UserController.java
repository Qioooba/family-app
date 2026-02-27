package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@SaCheckLogin
public class UserController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            // 从数据库获取真实用户信息
            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId);
            data.put("username", "user_" + userId);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户信息失败");
        }
        return result;
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String phone = (String) params.get("phone");
            if (phone == null || phone.isEmpty()) {
                result.put("code", 400);
                result.put("message", "手机号不能为空");
                return result;
            }
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", "test-token-" + System.currentTimeMillis());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登录失败");
        }
        return result;
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String phone = (String) params.get("phone");
            if (phone == null || phone.isEmpty()) {
                result.put("code", 400);
                result.put("message", "手机号不能为空");
                return result;
            }
            String nickname = (String) params.get("nickname");
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "id", System.currentTimeMillis() % 10000,
                "phone", phone,
                "nickname", nickname != null ? nickname : "新用户"
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "注册失败");
        }
        return result;
    }
}
