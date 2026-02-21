package com.family.family.service;

import com.family.family.entity.Moment;
import com.family.family.entity.MomentComment;

import java.util.List;
import java.util.Map;

/**
 * 动态服务
 */
public interface MomentService {
    
    Long create(Long userId, Object request);
    
    Map<String, Object> getFeed(Long familyId, Integer page, Integer size);
    
    Boolean like(Long momentId, Long userId);
    
    Long comment(Long momentId, Long userId, String content, Long replyTo);
    
    List<MomentComment> getComments(Long momentId);
    
    Boolean delete(Long momentId, Long userId);
}
