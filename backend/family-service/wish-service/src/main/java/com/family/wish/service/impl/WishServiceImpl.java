package com.family.wish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.wish.entity.Wish;
import com.family.wish.mapper.WishMapper;
import com.family.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
}
