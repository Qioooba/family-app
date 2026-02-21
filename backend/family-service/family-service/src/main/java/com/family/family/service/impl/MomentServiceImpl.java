package com.family.family.service.impl;

import com.family.family.entity.Moment;
import com.family.family.mapper.MomentMapper;
import com.family.family.service.MomentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 动态服务实现
 */
@Service
public class MomentServiceImpl implements MomentService {
    
    private final MomentMapper momentMapper;
    
    public MomentServiceImpl(MomentMapper momentMapper) {
        this.momentMapper = momentMapper;
    }
    
    @Override
    public Moment createMoment(Moment moment) {
        momentMapper.insert(moment);
        return moment;
    }
    
    @Override
    public List<Moment> getFamilyMoments(Long familyId, int page, int size) {
        return momentMapper.selectList(null);
    }
    
    @Override
    public void likeMoment(Long momentId, Long userId) {
    }
    
    @Override
    public void commentMoment(Long momentId, Long userId, String content) {
    }
    
    @Override
    public void deleteMoment(Long momentId) {
        momentMapper.deleteById(momentId);
    }
}
