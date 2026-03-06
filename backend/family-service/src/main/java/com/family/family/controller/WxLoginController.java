package com.family.family.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.common.annotation.RateLimit;
import com.family.family.entity.User;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.utils.WxDecryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Value("${weixin.appId}")
    private String wxAppId;

    @Value("${weixin.appSecret}")
    private String wxAppSecret;

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
            
            if (code == null || code.isEmpty()) {
                result.put("code", 400);
                result.put("message", "微信授权码不能为空");
                return result;
            }
            
            // 1. 用 code 换取 openid 和 session_key
            String wxResponse = WxDecryptUtil.getOpenidAndSessionKey(wxAppId, wxAppSecret, code);
            
            if (wxResponse == null || wxResponse.isEmpty()) {
                result.put("code", 401);
                result.put("message", "微信登录失败，无法获取用户信息");
                return result;
            }
            
            // 解析 openid 和 session_key
            String[] parts = wxResponse.split(",");
            if (parts.length < 2) {
                result.put("code", 401);
                result.put("message", "微信登录响应格式错误");
                return result;
            }
            
            String openid = parts[0];
            String sessionKey = parts[1];
            
            log.info("微信登录成功，openid: {}", openid);
            
            // 2. 根据 openid 查找用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getWxOpenid, openid);
            User user = userMapper.selectOne(queryWrapper);
            
            boolean isNewUser = false;
            
            // 3. 如不存在，自动创建新用户
            if (user == null) {
                log.info("用户不存在，创建新用户");
                isNewUser = true;
                
                user = new User();
                user.setWxOpenid(openid);
                // 生成随机用户名
                String randomUsername = "wx_" + UUID.randomUUID().toString().substring(0, 8);
                user.setUsername(randomUsername);
                // 生成随机昵称
                String randomNickname = "微信用户" + (int)(Math.random() * 10000);
                user.setNickname(randomNickname);
                // 无密码登录（密码为空）
                user.setPassword("");
                user.setStatus(1); // 正常状态
                user.setCurrentFamilyId(DEFAULT_FAMILY_ID); // 设置默认家庭
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                
                userMapper.insert(user);
                
                // 自动将用户加入默认家庭
                FamilyMember familyMember = new FamilyMember();
                familyMember.setFamilyId(DEFAULT_FAMILY_ID);
                familyMember.setUserId(user.getId());
                familyMember.setRole("member");
                familyMember.setNickname(randomNickname);
                familyMember.setJoinTime(LocalDateTime.now());
                familyMemberMapper.insert(familyMember);
                
                log.info("新用户创建成功，userId: {}", user.getId());
            } else {
                // 检查用户状态
                if (user.getStatus() != null && user.getStatus() == 0) {
                    result.put("code", 403);
                    result.put("message", "账号已被禁用");
                    return result;
                }
                
                // 确保用户有默认家庭
                if (user.getCurrentFamilyId() == null) {
                    user.setCurrentFamilyId(DEFAULT_FAMILY_ID);
                    // 检查是否已经是家庭成员，如果不是则添加
                    FamilyMember existingMember = familyMemberMapper.selectByUserIdAndFamilyId(user.getId(), DEFAULT_FAMILY_ID);
                    if (existingMember == null) {
                        FamilyMember familyMember = new FamilyMember();
                        familyMember.setFamilyId(DEFAULT_FAMILY_ID);
                        familyMember.setUserId(user.getId());
                        familyMember.setRole("member");
                        familyMember.setNickname(user.getNickname());
                        familyMember.setJoinTime(LocalDateTime.now());
                        familyMemberMapper.insert(familyMember);
                    }
                }
            }
            
            // 4. 执行 Sa-Token 登录
            StpUtil.login(user.getId());
            String tokenValue = StpUtil.getTokenValue();
            
            // 5. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 6. 返回 token 和用户信息
            result.put("code", 200);
            result.put("message", isNewUser ? "注册并登录成功" : "登录成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenValue);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            data.put("isNewUser", isNewUser);
            result.put("data", data);
            
            log.info("微信登录完成，userId: {}, isNewUser: {}", user.getId(), isNewUser);
            
        } catch (Exception e) {
            log.error("微信登录失败", e);
            result.put("code", 500);
            result.put("message", "登录失败：" + e.getMessage());
        }
        
        return result;
    }
}
