package com.family.family.controller;

import com.family.family.entity.User;
import com.family.family.mapper.UserMapper;
import com.family.family.util.TempTokenUtil;
import com.family.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 自动登录Controller - 处理免密登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TempTokenUtil tempTokenUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 临时Token免密登录
     */
    @PostMapping("/auto-login")
    public Result autoLogin(@RequestBody Map<String, String> request) {
        String tempToken = request.get("tempToken");
        
        if (tempToken == null || tempToken.isEmpty()) {
            return Result.error("登录凭证不能为空");
        }
        
        // 验证临时Token
        Long userId = tempTokenUtil.validateTempToken(tempToken);
        if (userId == null) {
            return Result.error("登录链接已过期，请重新获取");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 生成正式的登录Token（使用sa-token）
        cn.dev33.satoken.stp.StpUtil.login(userId);
        String token = cn.dev33.satoken.stp.StpUtil.getTokenValue();
        
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("username", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("currentFamilyId", user.getCurrentFamilyId());
        
        log.info("免密登录成功: userId={}", userId);
        return Result.success("登录成功", data);
    }

    /**
     * 验证临时Token（用于H5页面提前检查）
     */
    @GetMapping("/validate-temp-token")
    public Result validateTempToken(@RequestParam String token) {
        Long userId = tempTokenUtil.validateTempToken(token);
        if (userId == null) {
            return Result.error("Token无效或已过期");
        }
        return Result.success("Token有效", Map.of("userId", userId));
    }
}
