package com.family.wish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.wish.entity.Wish;

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
}
