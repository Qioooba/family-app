package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.family.controller.MomentController;
import com.family.family.entity.Moment;
import com.family.family.entity.MomentComment;
import com.family.family.entity.MomentLike;
import com.family.family.mapper.MomentCommentMapper;
import com.family.family.mapper.MomentLikeMapper;
import com.family.family.mapper.MomentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家庭圈动态服务
 */
@Service
public class MomentService {
    
    private final MomentMapper momentMapper;
    private final MomentLikeMapper momentLikeMapper;
    private final MomentCommentMapper momentCommentMapper;
    
    public MomentService(MomentMapper momentMapper, MomentLikeMapper momentLikeMapper, MomentCommentMapper momentCommentMapper) {
        this.momentMapper = momentMapper;
        this.momentLikeMapper = momentLikeMapper;
        this.momentCommentMapper = momentCommentMapper;
    }
    
    @Transactional
    public Long create(Long userId, MomentController.MomentCreateRequest request) {
        Moment moment = new Moment();
        moment.setFamilyId(request.getFamilyId());
        moment.setUserId(userId);
        moment.setContent(request.getContent());
        moment.setImages(request.getImages());
        moment.setLocation(request.getLocation());
        moment.setTags(request.getTags());
        moment.setMentions(request.getMentions());
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        moment.setStatus(1);
        moment.setCreateTime(LocalDateTime.now());
        momentMapper.insert(moment);
        return moment.getId();
    }
    
    public Map<String, Object> getFeed(Long familyId, Integer page, Integer size) {
        Page<Moment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Moment> wrapper = new LambdaQueryWrapper<Moment>()
                .eq(Moment::getFamilyId, familyId)
                .eq(Moment::getStatus, 1)
                .orderByDesc(Moment::getCreateTime);
        
        Page<Moment> result = momentMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return data;
    }
    
    @Transactional
    public Boolean like(Long momentId, Long userId) {
        LambdaQueryWrapper<MomentLike> wrapper = new LambdaQueryWrapper<MomentLike>()
                .eq(MomentLike::getMomentId, momentId)
                .eq(MomentLike::getUserId, userId);
        
        MomentLike existing = momentLikeMapper.selectOne(wrapper);
        
        if (existing != null) {
            momentLikeMapper.deleteById(existing.getId());
            momentMapper.decrementLikeCount(momentId);
            return false;
        } else {
            MomentLike like = new MomentLike();
            like.setMomentId(momentId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            momentLikeMapper.insert(like);
            momentMapper.incrementLikeCount(momentId);
            return true;
        }
    }
    
    @Transactional
    public Long comment(Long momentId, Long userId, String content, Long replyTo) {
        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setReplyTo(replyTo);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        momentCommentMapper.insert(comment);
        
        momentMapper.incrementCommentCount(momentId);
        return comment.getId();
    }
    
    public List<MomentComment> getComments(Long momentId) {
        LambdaQueryWrapper<MomentComment> wrapper = new LambdaQueryWrapper<MomentComment>()
                .eq(MomentComment::getMomentId, momentId)
                .orderByAsc(MomentComment::getCreateTime);
        return momentCommentMapper.selectList(wrapper);
    }
    
    @Transactional
    public Boolean delete(Long momentId, Long userId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null || !moment.getUserId().equals(userId)) {
            return false;
        }
        moment.setStatus(0);
        momentMapper.updateById(moment);
        return true;
    }
}
