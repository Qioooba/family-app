package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.InviteCode;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.FamilyMapper;
import com.family.family.mapper.InviteCodeMapper;
import com.family.family.service.InviteCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 邀请码服务实现
 */
@Service
public class InviteCodeServiceImpl extends ServiceImpl<InviteCodeMapper, InviteCode> implements InviteCodeService {
    
    @Autowired
    private FamilyMapper familyMapper;
    
    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    
    @Override
    public InviteCode createInviteCode(Long familyId, Long creatorId, Integer maxUses, Integer expireDays) {
        // 生成唯一邀请码
        String code = generateUniqueCode();
        
        InviteCode inviteCode = new InviteCode();
        inviteCode.setFamilyId(familyId);
        inviteCode.setCode(code);
        inviteCode.setCreatorId(creatorId);
        inviteCode.setMaxUses(maxUses != null ? maxUses : 1);
        inviteCode.setUsedCount(0);
        inviteCode.setStatus(1);
        
        if (expireDays != null && expireDays > 0) {
            inviteCode.setExpiresAt(LocalDateTime.now().plusDays(expireDays));
        }
        
        this.save(inviteCode);
        return inviteCode;
    }
    
    @Override
    public InviteCode verifyInviteCode(String code) {
        LambdaQueryWrapper<InviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InviteCode::getCode, code)
               .eq(InviteCode::getStatus, 1);
        
        InviteCode inviteCode = this.getOne(wrapper);
        
        if (inviteCode == null) {
            return null;
        }
        
        // 检查是否过期
        if (inviteCode.getExpiresAt() != null && 
            inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        // 检查使用次数
        if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            return null;
        }
        
        return inviteCode;
    }
    
    @Override
    @Transactional
    public Family joinFamilyByInviteCode(String code, Long userId) {
        InviteCode inviteCode = verifyInviteCode(code);
        if (inviteCode == null) {
            throw new RuntimeException("邀请码无效或已过期");
        }
        
        // 获取家庭信息
        Family family = familyMapper.selectById(inviteCode.getFamilyId());
        if (family == null) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查用户是否已经是家庭成员
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, family.getId())
               .eq(FamilyMember::getUserId, userId);
        FamilyMember existingMember = familyMemberMapper.selectOne(wrapper);
        
        if (existingMember != null) {
            throw new RuntimeException("您已经是家庭成员");
        }
        
        // 添加家庭成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole("member");
        member.setJoinTime(LocalDateTime.now());
        familyMemberMapper.insert(member);
        
        // 更新邀请码使用次数
        inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);
        this.updateById(inviteCode);
        
        // 更新家庭成员数
        family.setMemberCount(family.getMemberCount() != null ? family.getMemberCount() + 1 : 1);
        familyMapper.updateById(family);
        
        return family;
    }
    
    @Override
    public List<InviteCode> getFamilyInviteCodes(Long familyId) {
        LambdaQueryWrapper<InviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InviteCode::getFamilyId, familyId)
               .orderByDesc(InviteCode::getCreateTime);
        return this.list(wrapper);
    }
    
    @Override
    public void disableInviteCode(Long codeId) {
        InviteCode inviteCode = this.getById(codeId);
        if (inviteCode != null) {
            inviteCode.setStatus(0);
            this.updateById(inviteCode);
        }
    }
    
    @Override
    public boolean isAdmin(Long familyId, Long userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId)
               .eq(FamilyMember::getUserId, userId)
               .in(FamilyMember::getRole, "owner", "admin");
        
        FamilyMember member = familyMemberMapper.selectOne(wrapper);
        return member != null;
    }
    
    /**
     * 生成唯一邀请码（8位字母数字组合）
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (this.count(new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, code)) > 0);
        return code;
    }
}
