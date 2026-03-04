package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.dto.WxLoginRequest;
import com.family.family.entity.User;
import com.family.family.mapper.UserMapper;
import com.family.family.utils.WxDecryptUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "微信登录")
public class WxLoginController {

    @Autowired
    private UserMapper userMapper;

    @Value("${weixin.appId:wxyourappid}")
    private String appId;

    @Value("${weixin.appSecret:yourappsecret}")
    private String appSecret;

    /**
     * 微信手机号一键登录
     */
    @PostMapping("/wx-login")
    @Operation(summary = "微信手机号登录")
    public Result<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            log.info("微信登录请求: code={}", request.getWxCode());
            
            // 1. 通过 code 获取微信 session_key
            String openidAndSessionKey = WxDecryptUtil.getOpenidAndSessionKey(
                appId, appSecret, request.getWxCode());
            
            if (openidAndSessionKey == null || openidAndSessionKey.isEmpty()) {
                return Result.error("微信登录失败：无效的code");
            }
            
            String[] parts = openidAndSessionKey.split(",");
            if (parts.length < 2) {
                return Result.error("微信登录失败：解析openid失败");
            }
            
            String openid = parts[0];
            String sessionKey = parts[1];
            
            log.info("微信登录: openid={}", openid);
            
            // 2. 如果提供了手机号，加密数据解密获取手机号
            String phoneNumber = null;
            if (request.getEncryptedData() != null && request.getIv() != null) {
                phoneNumber = WxDecryptUtil.decryptPhoneNumber(
                    request.getEncryptedData(), sessionKey, request.getIv());
                log.info("解密手机号: {}", phoneNumber);
            }
            
            // 3. 根据 openid 查找用户
            User user = userMapper.selectByWxOpenid(openid);
            
            // 4. 如果用户不存在，自动创建新用户
            if (user == null) {
                // 创建新用户
                user = new User();
                user.setUsername("wx_" + openid.substring(0, 8));
                user.setPhone("");  // 空手机号
                user.setNickname("微信用户");
                user.setWxOpenid(openid);
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                user.setPassword(""); // 微信用户初始密码为空
                
                userMapper.insert(user);
                log.info("微信登录自动创建新用户: id={}, openid={}", user.getId(), openid);
            }
            
            // 5. 登录返回 token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("phone", user.getPhone());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("微信登录异常", e);
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 绑定已有账号（微信用户绑定手机号）
     */
    @PostMapping("/wx-bind-phone")
    @Operation(summary = "微信用户绑定手机号")
    public Result<Map<String, Object>> bindPhone(@RequestBody WxLoginRequest request) {
        try {
            if (request.getOpenid() == null || request.getPhone() == null) {
                return Result.error("参数不完整");
            }
            
            // 验证邀请码
            if (!"111222".equals(request.getCode())) {
                return Result.error("邀请码错误");
            }
            
            // 查找用户
            User user = userMapper.selectByPhone(request.getPhone());
            if (user == null) {
                return Result.error("该手机号未注册，请先注册");
            }
            
            // 检查是否已被绑定
            if (user.getWxOpenid() != null && !user.getWxOpenid().isEmpty()) {
                return Result.error("该手机号已绑定其他微信账号");
            }
            
            // 绑定 openid
            user.setWxOpenid(request.getOpenid());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 登录返回 token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("绑定手机号异常", e);
            return Result.error("绑定失败: " + e.getMessage());
        }
    }
}
