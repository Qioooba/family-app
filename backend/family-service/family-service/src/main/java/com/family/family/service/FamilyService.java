package com.family.family.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;

import java.util.List;

public interface FamilyService extends IService<Family> {
    
    Family createFamily(Family family, Long creatorId);
    
    Family joinFamily(String inviteCode, Long userId);
    
    List<FamilyMember> getFamilyMembers(Long familyId);
    
    void inviteMember(Long familyId, Long userId);
    
    void removeMember(Long familyId, Long userId);
    
    Family getFamilyById(Long id);
}
