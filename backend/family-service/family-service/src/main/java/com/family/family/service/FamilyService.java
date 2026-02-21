package com.family.family.service;

import com.family.family.entity.Family;

import java.util.List;

/**
 * 家庭服务
 */
public interface FamilyService {
    
    Family createFamily(Family family);
    
    Family getFamilyById(Long id);
    
    List<Family> getUserFamilies(Long userId);
    
    Family updateFamily(Family family);
    
    void deleteFamily(Long id);
    
    void joinFamily(Long familyId, Long userId);
    
    void leaveFamily(Long familyId, Long userId);
}
