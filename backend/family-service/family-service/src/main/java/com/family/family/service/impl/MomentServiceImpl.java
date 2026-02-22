package com.family.family.service.impl;

import com.family.common.core.PageResult;
import com.family.family.entity.Moment;
import com.family.family.entity.MomentComment;
import com.family.family.mapper.MomentCommentMapper;
import com.family.family.mapper.MomentMapper;
import com.family.family.service.MomentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态服务实现
 */
@Service
public class MomentServiceImpl implements MomentService {
    
    private final MomentMapper momentMapper;
    private final MomentCommentMapper momentCommentMapper;
    
    public MomentServiceImpl(MomentMapper momentMapper, 
                             MomentCommentMapper momentCommentMapper) {
        this.momentMapper = momentMapper;
        this.momentCommentMapper = momentCommentMapper;
    }
    
    @Override
    public PageResult<Moment> list(Long familyId, Long userId, Integer page, Integer size) {
        return PageResult.empty();
    }
    
    @Override
    public Moment create(Moment moment, Long userId) {
        momentMapper.insert(moment);
        return moment;
    }
    
    @Override
    public Long create(Long userId, Object request) {
        Moment moment = new Moment();
        momentMapper.insert(moment);
        return moment.getId();
    }
    
    @Override
    public Map<String, Object> getFeed(Long familyId, Integer page, Integer size) {
        Map<String, Object> feed = new HashMap<>();
        feed.put("list", new ArrayList<>());
        feed.put("total", 0);
        return feed;
    }
    
    @Override
    public Boolean like(Long momentId, Long userId) {
        return true;
    }
    
    @Override
    public MomentComment comment(MomentComment comment, Long userId) {
        momentCommentMapper.insert(comment);
        return comment;
    }
    
    @Override
    public Long comment(Long momentId, Long userId, String content, Long replyTo) {
        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setReplyTo(replyTo);
        momentCommentMapper.insert(comment);
        return comment.getId();
    }
    
    @Override
    public List<MomentComment> getComments(Long momentId) {
        return new ArrayList<>();
    }
    
    @Override
    public Moment detail(Long momentId, Long userId) {
        return momentMapper.selectById(momentId);
    }
    
    @Override
    public Boolean delete(Long momentId, Long userId) {
        momentMapper.deleteById(momentId);
        return true;
    }
}
