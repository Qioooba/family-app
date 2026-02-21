package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.User;
import com.family.family.mapper.FamilyMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 处理多家庭切换、用户设置等功能
 */
@RestController
@RequestMapping("/api/user")
@SaCheckLogin
@RequiredArgsConstructor
public class UserController {
    
    private final UserMapper userMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final FamilyMapper familyMapper;
    
    /**
     * 获取当前用户信息
     * GET /api/user/me
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userMapper.selectById(userId));
    }
    
    /**
     * 获取用户加入的所有家庭
     * GET /api/user/families
     */
    @GetMapping("/families")
    public Result<List<Family>> getMyFamilies() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户所有家庭成员记录
        List<FamilyMember> members = familyMemberMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
        );
        
        if (members.isEmpty()) {
            return Result.success(List.of());
        }
        
        // 获取家庭详情
        List<Long> familyIds = members.stream()
            .map(FamilyMember::getFamilyId)
            .collect(Collectors.toList());
        
        List<Family> families = familyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Family>()
                .in(Family::getId, familyIds)
        );
        
        return Result.success(families);
    }
    
    /**
     * 切换当前家庭
     * POST /api/user/switch-family
     */
    @PostMapping("/switch-family")
    public Result<Boolean> switchFamily(@RequestBody SwitchFamilyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证用户是否在该家庭中
        FamilyMember member = familyMemberMapper.selectByUserIdAndFamilyId(userId, request.getFamilyId());
        if (member == null) {
            return Result.error(403, "您不是该家庭的成员");
        }
        
        // 更新用户的当前家庭ID
        User user = userMapper.selectById(userId);
        user.setCurrentFamilyId(request.getFamilyId());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        return Result.success(true);
    }
    
    /**
     * 获取当前家庭
     * GET /api/user/current-family
     */
    @GetMapping("/current-family")
    public Result<Family> getCurrentFamily() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        
        if (user.getCurrentFamilyId() == null) {
            return Result.error("未选择当前家庭");
        }
        
        Family family = familyMapper.selectById(user.getCurrentFamilyId());
        return Result.success(family);
    }
    
    /**
     * 更新用户信息
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setId(userId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(true);
    }
    
    /**
     * 切换家庭请求
     */
    public static class SwitchFamilyRequest {
        private Long familyId;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
    }
}
