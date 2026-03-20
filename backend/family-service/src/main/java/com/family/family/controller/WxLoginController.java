package com.family.family.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.common.annotation.RateLimit;
import com.family.family.entity.User;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.service.SystemConfigService;
import com.family.family.utils.WxDecryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WxLoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private SystemConfigService configService;

    // 默认家庭ID - 所有用户自动加入这个家庭
    private static final Long DEFAULT_FAMILY_ID = 1L;

    /**
     * 微信一键登录
     * @param params 包含 code
     * @return token 和用户信息
     */
    @PostMapping("/wx-login")
    @SaIgnore
    @RateLimit(qps = 5.0, message = "登录请求过于频繁，请稍后再试")
    public Map<String, Object> wxLogin(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String code = (String) params.get("code");
            
            log.info("收到微信登录请求: code={}", code != null ? code.substring(0, Math.min(code.length(), 10)) + "..." : "null");
            
            if (code == null || code.isEmpty()) {
                log.warn("微信登录失败: code 为空");
                result.put("code", 400);
                result.put("message", "微信授权码不能为空");
                return result;
            }
            
            // 检查微信配置
            String wxAppId = configService.getWeixinAppId();
            String wxAppSecret = configService.getWeixinAppSecret();

            if (wxAppId == null || wxAppId.isEmpty()) {
                log.error("微信登录失败: 未配置 wxAppId");
                result.put("code", 500);
                result.put("message", "服务器配置错误：未配置微信 AppID");
                return result;
            }
            if (wxAppSecret == null || wxAppSecret.isEmpty()) {
                log.error("微信登录失败: 未配置 wxAppSecret");
                result.put("code", 500);
                result.put("message", "服务器配置错误：未配置微信 AppSecret");
                return result;
            }

            // 1. 用 code 换取 openid 和 session_key
            log.info("开始调用微信接口换取 openid...");
            String wxResponse = WxDecryptUtil.getOpenidAndSessionKey(wxAppId, wxAppSecret, code);
            
            if (wxResponse == null || wxResponse.isEmpty()) {
                log.error("微信登录失败: WxDecryptUtil 返回 null 或空");
                result.put("code", 401);
                result.put("message", "微信登录失败，无法获取用户信息");
                return result;
            }
            
            // 解析 openid 和 session_key
            String[] parts = wxResponse.split(",");
            if (parts.length < 1) {
                log.error("微信登录失败: 响应格式错误, wxResponse={}", wxResponse);
                result.put("code", 401);
                result.put("message", "微信登录响应格式错误");
                return result;
            }
            
            String openid = parts[0];
            String sessionKey = parts.length > 1 ? parts[1] : "";
            
            log.info("微信登录获取 openid 成功: openid={}", openid);
            
            // 2. 根据 openid 查找用户
            log.info("开始查询用户: openid={}", openid);
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getWxOpenid, openid);
            User user = userMapper.selectOne(queryWrapper);
            
            // 3. 如不存在，返回错误提示用户先注册
            if (user == null) {
                log.warn("微信登录失败: 用户未注册, openid={}", openid);
                result.put("code", 40401);
                result.put("message", "您还未注册，请先注册账号");
                result.put("needRegister", true);
                return result;
            }
            
            log.info("用户已存在: userId={}, status={}", user.getId(), user.getStatus());
            
            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                log.warn("用户已被禁用: userId={}", user.getId());
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 确保用户有默认家庭
            if (user.getCurrentFamilyId() == null) {
                log.info("用户没有默认家庭，设置为默认值: userId={}", user.getId());
                user.setCurrentFamilyId(DEFAULT_FAMILY_ID);
                // 检查是否已经是家庭成员，如果不是则添加
                FamilyMember existingMember = familyMemberMapper.selectByUserIdAndFamilyId(user.getId(), DEFAULT_FAMILY_ID);
                if (existingMember == null) {
                    log.info("添加现有用户到默认家庭: userId={}", user.getId());
                    FamilyMember familyMember = new FamilyMember();
                    familyMember.setFamilyId(DEFAULT_FAMILY_ID);
                    familyMember.setUserId(user.getId());
                    familyMember.setRole("member");
                    familyMember.setNickname(user.getNickname());
                    familyMember.setJoinTime(LocalDateTime.now());
                    familyMemberMapper.insert(familyMember);
                }
            }
            
            // 4. 执行 Sa-Token 登录
            log.info("执行 Sa-Token 登录: userId={}", user.getId());
            StpUtil.login(user.getId());
            String tokenValue = StpUtil.getTokenValue();
            log.info("Sa-Token 登录成功: userId={}", user.getId());
            
            // 5. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 6. 返回 token 和用户信息
            result.put("code", 200);
            result.put("message", "登录成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenValue);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            result.put("data", data);
            
            log.info("微信登录完成: userId={}", user.getId());
            
        } catch (Exception e) {
            log.error("微信登录异常: {}", e.getMessage(), e);
            result.put("code", 500);
            result.put("message", "登录失败：" + e.getMessage());
        }
        
        return result;
    }
}
