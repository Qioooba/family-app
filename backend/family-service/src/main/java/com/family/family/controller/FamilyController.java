package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.User;
import com.family.family.mapper.FamilyMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.UserMapper;
import com.family.family.service.InviteCodeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
@SaCheckLogin
public class FamilyController {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final InviteCodeService inviteCodeService;
    private final UserMapper userMapper;

    public FamilyController(FamilyMapper familyMapper,
                            FamilyMemberMapper familyMemberMapper,
                            InviteCodeService inviteCodeService,
                            UserMapper userMapper) {
        this.familyMapper = familyMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.inviteCodeService = inviteCodeService;
        this.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查用户是否为家庭管理员
     */
    private boolean isFamilyAdmin(Long familyId, Long userId) {
        if (familyId == null || userId == null) return false;
        return inviteCodeService.isAdmin(familyId, userId);
    }

    /**
     * 检查用户是否为家庭成员
     */
    private boolean isFamilyMember(Long familyId, Long userId) {
        if (familyId == null || userId == null) return false;
        FamilyMember member = familyMemberMapper.selectOne(
            new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
        );
        return member != null;
    }

    /**
     * 获取家庭列表
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 查询用户加入的所有家庭
            List<FamilyMember> memberships = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getUserId, userId)
            );

            List<Map<String, Object>> familyList = new ArrayList<>();
            for (FamilyMember membership : memberships) {
                Family family = familyMapper.selectById(membership.getFamilyId());
                if (family != null) {
                    Map<String, Object> familyData = new HashMap<>();
                    familyData.put("id", family.getId());
                    familyData.put("name", family.getName());
                    familyData.put("avatar", family.getAvatar());
                    familyData.put("memberCount", family.getMemberCount());
                    familyData.put("role", membership.getRole());
                    familyData.put("createTime", family.getCreateTime());
                    familyList.add(familyData);
                }
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", familyList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 获取家庭信息
     */
    @GetMapping("/info")
    public Map<String, Object> info(@RequestParam(required = false) Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        if (familyId == null) {
            result.put("code", 400);
            result.put("message", "familyId is required");
            return result;
        }

        // 检查是否为家庭成员
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            Family family = familyMapper.selectById(familyId);
            if (family == null) {
                result.put("code", 404);
                result.put("message", "家庭不存在");
                return result;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", family.getId());
            data.put("name", family.getName());
            data.put("avatar", family.getAvatar());
            data.put("memberCount", family.getMemberCount());
            data.put("createTime", family.getCreateTime());

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 创建家庭（第一个用户创建自动成为家长）
     */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long creatorId = getCurrentUserId();

        if (creatorId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            String name = data.get("name") != null ? data.get("name").toString() : "我的家庭";

            // 创建家庭
            Family family = new Family();
            family.setName(name);
            family.setCreatorId(creatorId);
            family.setMemberCount(1);
            family.setCreateTime(LocalDateTime.now());
            family.setUpdateTime(LocalDateTime.now());

            familyMapper.insert(family);

            // 创建者自动成为家长
            FamilyMember member = new FamilyMember();
            member.setFamilyId(family.getId());
            member.setUserId(creatorId);
            member.setRole("owner");
            member.setJoinTime(LocalDateTime.now());
            familyMemberMapper.insert(member);

            // 生成第一个邀请码
            inviteCodeService.createInviteCode(family.getId(), creatorId, 5, 30);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of("id", family.getId(), "name", family.getName()));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 更新家庭信息
     */
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Long familyId = Long.valueOf(data.get("id").toString());

            // 检查权限（只有管理员可以更新）
            if (!isFamilyAdmin(familyId, userId)) {
                result.put("code", 403);
                result.put("message", "只有家庭管理员可以更新家庭信息");
                return result;
            }

            Family family = familyMapper.selectById(familyId);
            if (family == null) {
                result.put("code", 404);
                result.put("message", "家庭不存在");
                return result;
            }

            if (data.containsKey("name")) {
                family.setName(data.get("name").toString());
            }
            if (data.containsKey("avatar")) {
                family.setAvatar(data.get("avatar").toString());
            }

            family.setUpdateTime(LocalDateTime.now());
            familyMapper.updateById(family);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 修改家庭名称（管理员可操作）
     */
    @PutMapping("/{id}/name")
    public Map<String, Object> updateFamilyName(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 检查权限（只有管理员可以修改）
            if (!isFamilyAdmin(id, userId)) {
                result.put("code", 403);
                result.put("message", "只有家庭管理员可以修改家庭名称");
                return result;
            }

            Family family = familyMapper.selectById(id);
            if (family == null) {
                result.put("code", 404);
                result.put("message", "家庭不存在");
                return result;
            }

            String name = data.get("name") != null ? data.get("name").toString() : null;
            if (name == null || name.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "家庭名称不能为空");
                return result;
            }

            family.setName(name.trim());
            family.setUpdateTime(LocalDateTime.now());
            familyMapper.updateById(family);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of("id", family.getId(), "name", family.getName()));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 删除家庭（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Family family = familyMapper.selectById(id);
            if (family == null) {
                result.put("code", 404);
                result.put("message", "家庭不存在");
                return result;
            }

            // 只有创建者可以删除家庭
            if (!family.getCreatorId().equals(userId)) {
                result.put("code", 403);
                result.put("message", "只有家庭创建者可以删除家庭");
                return result;
            }

            // 逻辑删除（将家庭名标记为已删除）
            family.setName(family.getName() + "（已删除）");
            family.setUpdateTime(LocalDateTime.now());
            familyMapper.updateById(family);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 获取家庭成员列表
     */
    @GetMapping("/{familyId}/members")
    public Map<String, Object> getMembers(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 检查是否为家庭成员
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            List<FamilyMember> members = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getFamilyId, familyId)
            );
            
            // 组装成员信息，包含用户头像
            List<Map<String, Object>> memberList = new ArrayList<>();
            for (FamilyMember member : members) {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("id", member.getId());
                memberInfo.put("familyId", member.getFamilyId());
                memberInfo.put("userId", member.getUserId());
                memberInfo.put("role", member.getRole());
                memberInfo.put("nickname", member.getNickname());
                memberInfo.put("joinTime", member.getJoinTime());
                
                // 获取用户头像
                User user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    memberInfo.put("avatar", user.getAvatar());
                    memberInfo.put("username", user.getUsername());
                }
                
                memberList.add(memberInfo);
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", memberList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 邀请成员加入家庭（已废弃，使用邀请码）
     */
    @PostMapping("/{familyId}/invite")
    public Map<String, Object> inviteMember(@PathVariable Long familyId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 410);
        result.put("message", "请使用邀请码功能邀请家人");
        return result;
    }

    /**
     * 移除家庭成员
     */
    @DeleteMapping("/{familyId}/members/{targetUserId}")
    public Map<String, Object> removeMember(@PathVariable Long familyId, @PathVariable Long targetUserId) {
        Map<String, Object> result = new HashMap<>();
        Long operatorId = getCurrentUserId();

        if (operatorId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 不能移除自己
            if (operatorId.equals(targetUserId)) {
                result.put("code", 400);
                result.put("message", "不能移除自己，如需退出请使用退出家庭功能");
                return result;
            }

            // 检查操作者权限
            if (!isFamilyAdmin(familyId, operatorId)) {
                result.put("code", 403);
                result.put("message", "只有家庭管理员可以移除成员");
                return result;
            }

            // 检查目标用户是否为家庭成员
            if (!isFamilyMember(familyId, targetUserId)) {
                result.put("code", 404);
                result.put("message", "该用户不是家庭成员");
                return result;
            }

            // 不能移除家庭创建者
            Family family = familyMapper.selectById(familyId);
            if (family != null && family.getCreatorId().equals(targetUserId)) {
                result.put("code", 403);
                result.put("message", "不能移除家庭创建者");
                return result;
            }

            // 执行移除
            familyMemberMapper.delete(
                new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getFamilyId, familyId)
                    .eq(FamilyMember::getUserId, targetUserId)
            );

            // 更新家庭成员数
            family.setMemberCount(family.getMemberCount() - 1);
            familyMapper.updateById(family);

            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }

    /**
     * 切换当前家庭
     */
    @PostMapping("/switch/{familyId}")
    public Map<String, Object> switchFamily(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 检查是否为家庭成员
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        result.put("code", 200);
        result.put("message", "success");
        return result;
    }

    /**
     * 获取家庭统计数据
     */
    @GetMapping("/{familyId}/statistics")
    public Map<String, Object> getStatistics(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 检查是否为家庭成员
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        result.put("code", 200);
        result.put("message", "success");
        result.put("data", new HashMap<>());
        return result;
    }

    /**
     * 检查用户是否为管理员
     */
    @GetMapping("/{familyId}/check-admin/{checkUserId}")
    public Map<String, Object> checkAdmin(@PathVariable Long familyId, @PathVariable Long checkUserId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId();

        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        // 检查是否为家庭成员
        if (!isFamilyMember(familyId, userId)) {
            result.put("code", 403);
            result.put("message", "您不是该家庭的成员");
            return result;
        }

        try {
            boolean isAdmin = isFamilyAdmin(familyId, checkUserId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of("isAdmin", isAdmin));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
        }

        return result;
    }
}
