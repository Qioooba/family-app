package com.family.wish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.wish.dto.WishMilestoneDTO;
import com.family.wish.entity.Wish;
import com.family.wish.entity.WishMilestone;
import com.family.wish.mapper.WishMilestoneMapper;
import com.family.wish.service.WishMilestoneService;
import com.family.wish.service.WishService;
import com.family.wish.vo.WishMilestoneVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 心愿里程碑服务实现
 */
@Service
@RequiredArgsConstructor
public class WishMilestoneServiceImpl extends ServiceImpl<WishMilestoneMapper, WishMilestone> implements WishMilestoneService {
    
    private final WishService wishService;
    
    @Override
    @Transactional
    public WishMilestoneVO addMilestone(Long wishId, WishMilestoneDTO dto) {
        // 验证心愿存在
        Wish wish = wishService.getById(wishId);
        if (wish == null) {
            throw new RuntimeException("心愿不存在");
        }
        
        WishMilestone milestone = new WishMilestone();
        milestone.setWishId(wishId);
        milestone.setTitle(dto.getTitle());
        milestone.setDescription(dto.getDescription());
        milestone.setTargetDate(dto.getTargetDate());
        milestone.setIsCompleted(0);
        milestone.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        
        save(milestone);
        
        // 更新心愿进度
        updateWishProgress(wishId);
        
        return convertToVO(milestone, wish.getTitle());
    }
    
    @Override
    @Transactional
    public void completeMilestone(Long milestoneId) {
        WishMilestone milestone = getById(milestoneId);
        if (milestone == null) {
            throw new RuntimeException("里程碑不存在");
        }
        
        milestone.setIsCompleted(1);
        updateById(milestone);
        
        // 更新心愿进度
        updateWishProgress(milestone.getWishId());
    }
    
    @Override
    public List<WishMilestoneVO> getMilestones(Long wishId) {
        Wish wish = wishService.getById(wishId);
        String wishTitle = wish != null ? wish.getTitle() : "";
        
        List<WishMilestone> milestones = lambdaQuery()
                .eq(WishMilestone::getWishId, wishId)
                .orderByAsc(WishMilestone::getSortOrder)
                .orderByDesc(WishMilestone::getCreateTime)
                .list();
        
        return milestones.stream()
                .map(m -> convertToVO(m, wishTitle))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteMilestone(Long milestoneId) {
        WishMilestone milestone = getById(milestoneId);
        if (milestone == null) {
            return;
        }
        
        Long wishId = milestone.getWishId();
        removeById(milestoneId);
        
        // 更新心愿进度
        updateWishProgress(wishId);
    }
    
    @Override
    @Transactional
    public void updateWishProgress(Long wishId) {
        // 查询该心愿的所有里程碑
        List<WishMilestone> milestones = lambdaQuery()
                .eq(WishMilestone::getWishId, wishId)
                .list();
        
        if (milestones.isEmpty()) {
            return;
        }
        
        // 计算完成百分比
        long completedCount = milestones.stream()
                .filter(m -> m.getIsCompleted() != null && m.getIsCompleted() == 1)
                .count();
        
        int progress = (int) ((completedCount * 100) / milestones.size());
        
        // 更新心愿进度
        wishService.updateProgress(wishId, progress);
    }
    
    private WishMilestoneVO convertToVO(WishMilestone milestone, String wishTitle) {
        WishMilestoneVO vo = new WishMilestoneVO();
        vo.setId(milestone.getId());
        vo.setWishId(milestone.getWishId());
        vo.setWishTitle(wishTitle);
        vo.setTitle(milestone.getTitle());
        vo.setDescription(milestone.getDescription());
        vo.setTargetDate(milestone.getTargetDate());
        vo.setIsCompleted(milestone.getIsCompleted());
        vo.setSortOrder(milestone.getSortOrder());
        vo.setCreateTime(milestone.getCreateTime());
        return vo;
    }
}