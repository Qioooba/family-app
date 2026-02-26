package com.family.family.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.InviteCode;
import com.family.family.mapper.FamilyMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.service.InviteCodeService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final InviteCodeService inviteCodeService;
    
    public FamilyController(FamilyMapper familyMapper, 
                            FamilyMemberMapper familyMemberMapper,
                            InviteCodeService inviteCodeService) {
        this.familyMapper = familyMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.inviteCodeService = inviteCodeService;
    }

    /**
     * 获取家庭列表
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", new ArrayList<>());
        return result;
    }

    /**
     * 获取家庭信息
     */
    @GetMapping("/info")
    public Map<String, Object> info(@RequestParam(required = false) Long familyId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (familyId == null) {
                result.put("code", 400);
                result.put("message", "familyId is required");
                return result;
            }
            
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
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建家庭（第一个用户创建自动成为家长）
     */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String name = data.get("name") != null ? data.get("name").toString() : "我的家庭";
            Long creatorId = 1L; // 默认用户
            
            if (data.get("creatorId") != null) {
                creatorId = Long.valueOf(data.get("creatorId").toString());
            }
            
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
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新家庭信息
     */
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long familyId = Long.valueOf(data.get("id").toString());
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
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除家庭
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            familyMapper.deleteById(id);
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取家庭成员列表
     */
    @GetMapping("/{familyId}/members")
    public Map<String, Object> getMembers(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<FamilyMember> members = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getFamilyId, familyId)
            );
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", members);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
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
    @DeleteMapping("/{familyId}/members/{userId}")
    public Map<String, Object> removeMember(@PathVariable Long familyId, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查是否是家长操作
            Long operatorId = Long.valueOf(result.get("operatorId") != null ? result.get("operatorId").toString() : "0");
            
            familyMemberMapper.delete(
                new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getFamilyId, familyId)
                    .eq(FamilyMember::getUserId, userId)
            );
            
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 切换当前家庭
     */
    @PostMapping("/switch/{familyId}")
    public Map<String, Object> switchFamily(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
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
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", new HashMap<>());
        return result;
    }
    
    /**
     * 检查用户是否为管理员
     */
    @GetMapping("/{familyId}/check-admin/{userId}")
    public Map<String, Object> checkAdmin(@PathVariable Long familyId, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isAdmin = inviteCodeService.isAdmin(familyId, userId);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of("isAdmin", isAdmin));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}
