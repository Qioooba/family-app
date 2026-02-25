package com.family.wish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.wish.dto.WishBudgetDTO;
import com.family.wish.entity.Wish;
import com.family.wish.vo.BudgetStatsVO;

import java.util.List;

public interface WishService extends IService<Wish> {
    
    List<Wish> getFamilyWishes(Long familyId, String type, Integer status);
    
    Wish createWish(Wish wish);
    
    Wish updateWish(Wish wish);
    
    void claimWish(Long wishId, Long userId);
    
    void updateProgress(Long wishId, Integer progress);
    
    void completeWish(Long wishId);
    
    void abandonWish(Long wishId);
    
    List<Wish> getUserCompletedWishes(Long userId);
    
    /**
     * 设置心愿预算
     * @param wishId 心愿ID
     * @param dto 预算信息
     */
    void setBudget(Long wishId, WishBudgetDTO dto);
    
    /**
     * 获取家庭预算统计
     * @param familyId 家庭ID
     * @return 预算统计
     */
    BudgetStatsVO getBudgetStats(Long familyId);
}
