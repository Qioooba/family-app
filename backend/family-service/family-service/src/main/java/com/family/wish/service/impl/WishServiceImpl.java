package com.family.wish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.wish.dto.WishBudgetDTO;
import com.family.family.entity.Wish;
import com.family.wish.mapper.WishMapper;
import com.family.wish.service.WishService;
import com.family.wish.vo.BudgetStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishServiceImpl extends ServiceImpl<WishMapper, Wish> implements WishService {
    
    @Override
    public List<Wish> getFamilyWishes(Long familyId, String type, Integer status) {
        return lambdaQuery()
                .eq(Wish::getFamilyId, familyId)
                .eq(type != null, Wish::getType, type)
                .eq(status != null, Wish::getStatus, status)
                .ne(Wish::getStatus, 3)
                .orderByDesc(Wish::getPriority)
                .orderByDesc(Wish::getCreateTime)
                .list();
    }
    
    @Override
    public Wish createWish(Wish wish) {
        wish.setStatus(0);
        wish.setProgress(0);
        save(wish);
        return wish;
    }
    
    @Override
    public Wish updateWish(Wish wish) {
        updateById(wish);
        return wish;
    }
    
    @Override
    public void claimWish(Long wishId, Long userId) {
        Wish wish = getById(wishId);
        wish.setClaimantId(userId);
        wish.setStatus(1);
        updateById(wish);
    }
    
    @Override
    public void updateProgress(Long wishId, Integer progress) {
        Wish wish = getById(wishId);
        wish.setProgress(progress);
        if (progress >= 100) {
            wish.setStatus(2);
            wish.setFinishTime(LocalDate.now());
        }
        updateById(wish);
    }
    
    @Override
    public void completeWish(Long wishId) {
        Wish wish = getById(wishId);
        wish.setStatus(2);
        wish.setProgress(100);
        wish.setFinishTime(LocalDate.now());
        updateById(wish);
    }
    
    @Override
    public void abandonWish(Long wishId) {
        Wish wish = getById(wishId);
        wish.setStatus(3);
        updateById(wish);
    }
    
    @Override
    public List<Wish> getUserCompletedWishes(Long userId) {
        return baseMapper.selectCompletedByUserId(userId);
    }
    
    @Override
    public void setBudget(Long wishId, WishBudgetDTO dto) {
        Wish wish = getById(wishId);
        if (wish == null) {
            throw new RuntimeException("心愿不存在");
        }
        wish.setBudgetMin(dto.getBudgetMin());
        wish.setBudgetMax(dto.getBudgetMax());
        updateById(wish);
    }
    
    @Override
    public BudgetStatsVO getBudgetStats(Long familyId) {
        List<Wish> wishes = lambdaQuery()
                .eq(Wish::getFamilyId, familyId)
                .ne(Wish::getStatus, 3)
                .list();
        
        BudgetStatsVO vo = new BudgetStatsVO();
        vo.setTotalWishCount(wishes.size());
        vo.setPendingCount((int) wishes.stream().filter(w -> w.getStatus() == 0).count());
        vo.setInProgressCount((int) wishes.stream().filter(w -> w.getStatus() == 1).count());
        vo.setCompletedCount((int) wishes.stream().filter(w -> w.getStatus() == 2).count());
        vo.setTotalBudgetMin(BigDecimal.ZERO);
        vo.setTotalBudgetMax(BigDecimal.ZERO);
        vo.setCompletedCost(BigDecimal.ZERO);
        vo.setEstimatedCost(BigDecimal.ZERO);
        vo.setBudgetByType(new ArrayList<>());
        return vo;
    }
}
