package com.family.family.service;

import com.family.family.entity.Moment;

import java.util.List;

/**
 * 动态服务
 */
public interface MomentService {
    
    Moment createMoment(Moment moment);
    
    List<Moment> getFamilyMoments(Long familyId, int page, int size);
    
    void likeMoment(Long momentId, Long userId);
    
    void commentMoment(Long momentId, Long userId, String content);
    
    void deleteMoment(Long momentId);
}
