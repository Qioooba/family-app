package com.family.family.service;

import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;

import java.util.List;

/**
 * 家庭服务
 */
public interface FamilyService {
    
    Family createFamily(Family family, Long creatorId);
    
    Family joinFamily(String inviteCode, Long userId);
    
    Family getFamilyById(Long id);
    
    List<Family> getUserFamilies(Long userId);
    
    Family updateFamily(Family family);
    
    void deleteFamily(Long id);
    
    void joinFamily(Long familyId, Long userId);
    
    void leaveFamily(Long familyId, Long userId);
    
    List<FamilyMember> getFamilyMembers(Long familyId);
    
    void removeMember(Long familyId, Long userId);
}
