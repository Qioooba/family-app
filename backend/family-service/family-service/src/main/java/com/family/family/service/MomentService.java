package com.family.family.service;

import com.family.common.core.PageResult;
import com.family.family.entity.Moment;
import com.family.family.entity.MomentComment;

import java.util.List;
import java.util.Map;

/**
 * 动态服务
 */
public interface MomentService {
    
    /**
     * 获取动态列表
     */
    PageResult<Moment> list(Long familyId, Long userId, Integer page, Integer size);
    
    /**
     * 创建动态
     */
    Moment create(Moment moment, Long userId);
    
    Long create(Long userId, Object request);
    
    Map<String, Object> getFeed(Long familyId, Integer page, Integer size);
    
    /**
     * 点赞动态
     */
    Boolean like(Long momentId, Long userId);
    
    /**
     * 评论动态
     */
    MomentComment comment(MomentComment comment, Long userId);
    
    Long comment(Long momentId, Long userId, String content, Long replyTo);
    
    List<MomentComment> getComments(Long momentId);
    
    /**
     * 获取动态详情
     */
    Moment detail(Long momentId, Long userId);
    
    /**
     * 删除动态
     */
    Boolean delete(Long momentId, Long userId);
}
