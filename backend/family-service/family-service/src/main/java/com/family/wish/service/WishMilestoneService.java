package com.family.wish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.wish.dto.WishMilestoneDTO;
import com.family.wish.entity.WishMilestone;
import com.family.wish.vo.WishMilestoneVO;

import java.util.List;

/**
 * 心愿里程碑服务
 */
public interface WishMilestoneService extends IService<WishMilestone> {
    
    /**
     * 添加里程碑
     * @param wishId 心愿ID
     * @param dto 里程碑信息
     * @return 里程碑VO
     */
    WishMilestoneVO addMilestone(Long wishId, WishMilestoneDTO dto);
    
    /**
     * 完成里程碑
     * @param milestoneId 里程碑ID
     */
    void completeMilestone(Long milestoneId);
    
    /**
     * 获取心愿的里程碑列表
     * @param wishId 心愿ID
     * @return 里程碑列表
     */
    List<WishMilestoneVO> getMilestones(Long wishId);
    
    /**
     * 删除里程碑
     * @param milestoneId 里程碑ID
     */
    void deleteMilestone(Long milestoneId);
    
    /**
     * 更新里程碑进度（自动更新心愿进度）
     * @param wishId 心愿ID
     */
    void updateWishProgress(Long wishId);
}