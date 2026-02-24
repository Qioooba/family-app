package com.family.family.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.InviteCode;

import java.util.List;

/**
 * 邀请码服务接口
 */
public interface InviteCodeService extends IService<InviteCode> {
    
    /**
     * 创建邀请码
     */
    InviteCode createInviteCode(Long familyId, Long creatorId, Integer maxUses, Integer expireDays);
    
    /**
     * 验证邀请码
     */
    InviteCode verifyInviteCode(String code);
    
    /**
     * 使用邀请码加入家庭
     */
    Family joinFamilyByInviteCode(String code, Long userId);
    
    /**
     * 获取家庭邀请码列表
     */
    List<InviteCode> getFamilyInviteCodes(Long familyId);
    
    /**
     * 禁用邀请码
     */
    void disableInviteCode(Long codeId);
    
    /**
     * 检查用户是否为家长/管理员
     */
    boolean isAdmin(Long familyId, Long userId);
}
