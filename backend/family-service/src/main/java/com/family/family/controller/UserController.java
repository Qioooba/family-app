package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
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
    @SaCheckLogin
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String tokenValue = StpUtil.getTokenValue();

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId);
            data.put("username", "user_" + userId);
            data.put("nickname", "用户" + userId);
            data.put("token", tokenValue);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", "未登录或登录已过期");
        }
        return result;
    }

    @PostMapping("/login")
    @SaIgnore
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String phone = (String) params.get("phone");
            if (phone == null || phone.isEmpty()) {
                result.put("code", 400);
                result.put("message", "手机号不能为空");
                return result;
            }

            // 模拟用户ID（实际应从数据库查询）
            Long userId = 1L;

            // 执行Sa-Token登录
            StpUtil.login(userId);
            String tokenValue = StpUtil.getTokenValue();

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", tokenValue);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登录失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/register")
    @SaIgnore
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
            result.put("message", "注册失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        try {
            StpUtil.logout();
            result.put("code", 200);
            result.put("message", "退出成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "退出失败：" + e.getMessage());
        }
        return result;
    }
}
