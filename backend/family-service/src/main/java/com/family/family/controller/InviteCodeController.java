package com.family.family.controller;

import com.family.family.entity.Family;
import com.family.family.entity.InviteCode;
import com.family.family.service.InviteCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邀请码控制器
 */
@RestController
@RequestMapping("/api/family")
public class InviteCodeController {
    
    private final InviteCodeService inviteCodeService;
    
    public InviteCodeController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }
    
    /**
     * 创建邀请码
     */
    @PostMapping("/invite-code")
    public Map<String, Object> createInviteCode(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long familyId = Long.valueOf(data.get("familyId").toString());
            Long creatorId = Long.valueOf(data.get("creatorId").toString());
            Integer maxUses = data.get("maxUses") != null ? Integer.valueOf(data.get("maxUses").toString()) : 1;
            Integer expireDays = data.get("expireDays") != null ? Integer.valueOf(data.get("expireDays").toString()) : 7;
            
            // 检查权限
            if (!inviteCodeService.isAdmin(familyId, creatorId)) {
                result.put("code", 403);
                result.put("message", "只有家长/管理员才能创建邀请码");
                return result;
            }
            
            InviteCode inviteCode = inviteCodeService.createInviteCode(familyId, creatorId, maxUses, expireDays);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "id", inviteCode.getId(),
                "code", inviteCode.getCode(),
                "maxUses", inviteCode.getMaxUses(),
                "usedCount", inviteCode.getUsedCount(),
                "expiresAt", inviteCode.getExpiresAt() != null ? inviteCode.getExpiresAt().toString() : null
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证邀请码
     */
    @PostMapping("/verify-code")
    public Map<String, Object> verifyInviteCode(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String code = data.get("code").toString();
            
            InviteCode inviteCode = inviteCodeService.verifyInviteCode(code);
            
            if (inviteCode == null) {
                result.put("code", 404);
                result.put("message", "邀请码无效或已过期");
                return result;
            }
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "valid", true,
                "familyId", inviteCode.getFamilyId()
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 使用邀请码加入家庭
     */
    @PostMapping("/join-by-code")
    public Map<String, Object> joinFamilyByCode(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查必要参数
            if (data.get("code") == null || data.get("code").toString().isEmpty()) {
                result.put("code", 400);
                result.put("message", "邀请码不能为空");
                return result;
            }
            
            String code = data.get("code").toString();
            
            // 如果没有提供userId，使用当前登录用户
            Long userId = null;
            if (data.get("userId") != null && !data.get("userId").toString().isEmpty()) {
                userId = Long.valueOf(data.get("userId").toString());
            }
            
            if (userId == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                return result;
            }
            
            Family family = inviteCodeService.joinFamilyByInviteCode(code, userId);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "familyId", family.getId(),
                "familyName", family.getName()
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取家庭邀请码列表
     */
    @GetMapping("/{familyId}/invite-codes")
    public Map<String, Object> getInviteCodes(@PathVariable Long familyId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<InviteCode> codes = inviteCodeService.getFamilyInviteCodes(familyId);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", codes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 禁用邀请码
     */
    @DeleteMapping("/invite-code/{codeId}")
    public Map<String, Object> disableInviteCode(@PathVariable Long codeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            inviteCodeService.disableInviteCode(codeId);
            
            result.put("code", 200);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查用户是否为管理员
     */
    @GetMapping("/{familyId}/is-admin/{userId}")
    public Map<String, Object> isAdmin(@PathVariable Long familyId, @PathVariable Long userId) {
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
