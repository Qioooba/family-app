package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.User;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.FamilyMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // 默认家庭ID - 所有用户自动加入这个家庭
    private static final Long DEFAULT_FAMILY_ID = 1L;

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
            // 获取登录凭证
            String phone = (String) params.get("phone");
            String username = (String) params.get("username");
            String password = (String) params.get("password");

            // 兼容处理：如果 phone 为空，尝试使用 username
            if (phone == null || phone.isEmpty()) {
                phone = username;
            }

            // 校验参数
            if ((phone == null || phone.isEmpty()) && (username == null || username.isEmpty())) {
                result.put("code", 400);
                result.put("message", "手机号/用户名不能为空");
                return result;
            }

            if (password == null || password.isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }

            // 根据 phone 或 username 查询用户
            User user = null;
            if (phone != null && !phone.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, phone);
                user = userMapper.selectOne(queryWrapper);
            }

            if (user == null && username != null && !username.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username);
                user = userMapper.selectOne(queryWrapper);
            }

            // 用户不存在
            if (user == null) {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
                return result;
            }

            // 校验密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                result.put("code", 401);
                result.put("message", "用户密码未设置，请联系管理员");
                return result;
            }

            // DEBUG: 打印调试信息
            System.out.println("===== DEBUG LOG =====");
            System.out.println("Input username: " + username);
            System.out.println("Input phone: " + phone);
            System.out.println("Input password: " + password);
            System.out.println("DB user id: " + user.getId());
            System.out.println("DB username: " + user.getUsername());
            System.out.println("DB phone: " + user.getPhone());
            System.out.println("DB password: " + user.getPassword());
            System.out.println("DB status: " + user.getStatus());
            System.out.println("BCrypt matches: " + passwordEncoder.matches(password, user.getPassword()));
            System.out.println("=====================");

            if (!passwordEncoder.matches(password, user.getPassword())) {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
                return result;
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }

            // 检查并设置默认家庭（如果没有current_family_id）
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

            // 执行Sa-Token登录
            StpUtil.login(user.getId());
            String tokenValue = StpUtil.getTokenValue();

            // 更新最后登录时间和current_family_id（如果需要）
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenValue);
            data.put("userId", user.getId());
            data.put("nickname", user.getNickname());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            result.put("data", data);
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
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String nickname = (String) params.get("nickname");

            // 校验手机号或用户名
            if ((phone == null || phone.isEmpty()) && (username == null || username.isEmpty())) {
                result.put("code", 400);
                result.put("message", "手机号或用户名至少填写一个");
                return result;
            }

            // 校验密码
            if (password == null || password.isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }

            // 检查手机号是否已存在
            if (phone != null && !phone.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, phone);
                User existingUser = userMapper.selectOne(queryWrapper);
                if (existingUser != null) {
                    result.put("code", 409);
                    result.put("message", "手机号已被注册");
                    return result;
                }
            }

            // 检查用户名是否已存在
            if (username != null && !username.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username);
                User existingUser = userMapper.selectOne(queryWrapper);
                if (existingUser != null) {
                    result.put("code", 409);
                    result.put("message", "用户名已被注册");
                    return result;
                }
            }

            // 创建新用户
            User user = new User();
            user.setPhone(phone);
            user.setUsername(username != null ? username : phone);
            user.setNickname(nickname != null ? nickname : (phone != null ? phone : username));
            user.setPassword(passwordEncoder.encode(password));
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
            familyMember.setNickname(user.getNickname());
            familyMember.setJoinTime(LocalDateTime.now());
            familyMemberMapper.insert(familyMember);

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("phone", user.getPhone());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("currentFamilyId", user.getCurrentFamilyId());
            result.put("data", data);
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
