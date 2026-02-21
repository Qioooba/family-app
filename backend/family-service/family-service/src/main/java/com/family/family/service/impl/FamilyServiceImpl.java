package com.family.family.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.FamilyMapper;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl extends ServiceImpl<FamilyMapper, Family> implements FamilyService {
    
    private final FamilyMemberMapper memberMapper;
    
    @Override
    @Transactional
    public Family createFamily(Family family, Long creatorId) {
        family.setCreatorId(creatorId);
        family.setInviteCode(RandomUtil.randomNumbers(8));
        family.setMemberCount(1);
        family.setStorageUsed(0L);
        family.setStorageLimit(10737418240L);
        save(family);
        
        // 创建者自动成为家主
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(creatorId);
        member.setRole("owner");
        memberMapper.insert(member);
        
        return family;
    }
    
    @Override
    @Transactional
    public Family joinFamily(String inviteCode, Long userId) {
        Family family = lambdaQuery().eq(Family::getInviteCode, inviteCode).one();
        if (family == null) {
            throw new RuntimeException("邀请码无效");
        }
        
        // 检查是否已是成员
        Long count = memberMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, family.getId())
                .eq(FamilyMember::getUserId, userId)
        );
        if (count > 0) {
            throw new RuntimeException("您已是该家庭成员");
        }
        
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole("member");
        memberMapper.insert(member);
        
        // 更新成员数
        family.setMemberCount(family.getMemberCount() + 1);
        updateById(family);
        
        return family;
    }
    
    @Override
    public List<FamilyMember> getFamilyMembers(Long familyId) {
        return memberMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
        );
    }
    
    @Override
    public void inviteMember(Long familyId, Long userId) {
        // 生成邀请链接或二维码
    }
    
    @Override
    @Transactional
    public void removeMember(Long familyId, Long userId) {
        memberMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
        );
        
        Family family = getById(familyId);
        family.setMemberCount(family.getMemberCount() - 1);
        updateById(family);
    }
    
    @Override
    public Family getFamilyById(Long id) {
        return getById(id);
    }
}
