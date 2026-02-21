package com.family.wish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.wish.dto.WishBudgetDTO;
import com.family.wish.entity.Wish;
import com.family.wish.mapper.WishMapper;
import com.family.wish.service.WishService;
import com.family.wish.vo.BudgetStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 获取家庭所有心愿
        List<Wish> wishes = lambdaQuery()
                .eq(Wish::getFamilyId, familyId)
                .ne(Wish::getStatus, 3) // 排除已放弃的
                .list();
        
        BudgetStatsVO vo = new BudgetStatsVO();
        
        // 基本统计
        vo.setTotalWishCount(wishes.size());
        vo.setPendingCount((int) wishes.stream().filter(w -> w.getStatus() == 0).count());
        vo.setInProgressCount((int) wishes.stream().filter(w -> w.getStatus() == 1).count());
        vo.setCompletedCount((int) wishes.stream().filter(w -> w.getStatus() == 2).count());
        
        // 预算统计
        BigDecimal totalBudgetMin = BigDecimal.ZERO;
        BigDecimal totalBudgetMax = BigDecimal.ZERO;
        BigDecimal completedCost = BigDecimal.ZERO;
        BigDecimal estimatedCost = BigDecimal.ZERO;
        
        for (Wish wish : wishes) {
            BigDecimal min = wish.getBudgetMin() != null ? wish.getBudgetMin() : BigDecimal.ZERO;
            BigDecimal max = wish.getBudgetMax() != null ? wish.getBudgetMax() : BigDecimal.ZERO;
            
            totalBudgetMin = totalBudgetMin.add(min);
            totalBudgetMax = totalBudgetMax.add(max);
            
            if (wish.getStatus() == 2) {
                // 已完成的使用实际预算平均值
                BigDecimal avgCost = min.add(max).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                completedCost = completedCost.add(avgCost);
            } else {
                // 待完成的使用预计平均值
                BigDecimal avgEstimate = min.add(max).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                estimatedCost = estimatedCost.add(avgEstimate);
            }
        }
        
        vo.setTotalBudgetMin(totalBudgetMin);
        vo.setTotalBudgetMax(totalBudgetMax);
        vo.setCompletedCost(completedCost);
        vo.setEstimatedCost(estimatedCost.add(completedCost));
        
        // 按类型统计
        Map<String, List<Wish>> wishesByType = wishes.stream()
                .collect(Collectors.groupingBy(Wish::getType));
        
        List<BudgetStatsVO.BudgetByTypeVO> budgetByTypeList = new ArrayList<>();
        for (Map.Entry<String, List<Wish>> entry : wishesByType.entrySet()) {
            BudgetStatsVO.BudgetByTypeVO typeVo = new BudgetStatsVO.BudgetByTypeVO();
            typeVo.setType(entry.getKey());
            typeVo.setTypeName(getTypeName(entry.getKey()));
            typeVo.setCount(entry.getValue().size());
            
            BigDecimal typeMin = entry.getValue().stream()
                    .map(w -> w.getBudgetMin() != null ? w.getBudgetMin() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal typeMax = entry.getValue().stream()
                    .map(w -> w.getBudgetMax() != null ? w.getBudgetMax() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            typeVo.setBudgetMin(typeMin);
            typeVo.setBudgetMax(typeMax);
            budgetByTypeList.add(typeVo);
        }
        vo.setBudgetByType(budgetByTypeList);
        
        return vo;
    }
    
    private String getTypeName(String type) {
        return switch (type) {
            case "item" -> "物品";
            case "experience" -> "体验";
            case "learn" -> "学习";
            case "relation" -> "关系";
            case "charity" -> "公益";
            case "goal" -> "目标";
            default -> "其他";
        };
    }
}
