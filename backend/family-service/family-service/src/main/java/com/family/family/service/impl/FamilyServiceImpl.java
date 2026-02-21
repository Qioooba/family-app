package com.family.family.service.impl;

import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.FamilyMapper;
import com.family.family.service.FamilyService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 家庭服务实现
 */
@Service
public class FamilyServiceImpl implements FamilyService {
    
    private final FamilyMapper familyMapper;
    
    public FamilyServiceImpl(FamilyMapper familyMapper) {
        this.familyMapper = familyMapper;
    }
    
    @Override
    public Family createFamily(Family family, Long creatorId) {
        family.setCreatorId(creatorId);
        familyMapper.insert(family);
        return family;
    }
    
    @Override
    public Family joinFamily(String inviteCode, Long userId) {
        return new Family();
    }
    
    @Override
    public Family getFamilyById(Long id) {
        return familyMapper.selectById(id);
    }
    
    @Override
    public List<Family> getUserFamilies(Long userId) {
        return familyMapper.selectList(null);
    }
    
    @Override
    public Family updateFamily(Family family) {
        familyMapper.updateById(family);
        return family;
    }
    
    @Override
    public void deleteFamily(Long id) {
        familyMapper.deleteById(id);
    }
    
    @Override
    public void joinFamily(Long familyId, Long userId) {
    }
    
    @Override
    public void leaveFamily(Long familyId, Long userId) {
    }
    
    @Override
    public List<FamilyMember> getFamilyMembers(Long familyId) {
        return new ArrayList<>();
    }
    
    @Override
    public void removeMember(Long familyId, Long userId) {
    }
}
