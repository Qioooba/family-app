package com.family.wish.controller;

import com.family.common.core.Result;
import com.family.wish.dto.WishBudgetDTO;
import com.family.wish.entity.Wish;
import com.family.wish.service.WishService;
import com.family.wish.vo.BudgetStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wish")
@RequiredArgsConstructor
public class WishController {
    
    private final WishService wishService;
    
    @GetMapping("/list")
    public Result<List<Wish>> list(@RequestParam Long familyId,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) Integer status) {
        return Result.success(wishService.getFamilyWishes(familyId, type, status));
    }
    
    @PostMapping("/create")
    public Result<Wish> create(@RequestBody Wish wish) {
        return Result.success(wishService.createWish(wish));
    }
    
    @PutMapping("/update")
    public Result<Wish> update(@RequestBody Wish wish) {
        return Result.success(wishService.updateWish(wish));
    }
    
    @PostMapping("/claim/{wishId}")
    public Result<Void> claim(@PathVariable Long wishId, @RequestParam Long userId) {
        wishService.claimWish(wishId, userId);
        return Result.success();
    }
    
    @PostMapping("/progress/{wishId}")
    public Result<Void> updateProgress(@PathVariable Long wishId, @RequestParam Integer progress) {
        wishService.updateProgress(wishId, progress);
        return Result.success();
    }
    
    @PostMapping("/complete/{wishId}")
    public Result<Void> complete(@PathVariable Long wishId) {
        wishService.completeWish(wishId);
        return Result.success();
    }
    
    @PostMapping("/abandon/{wishId}")
    public Result<Void> abandon(@PathVariable Long wishId) {
        wishService.abandonWish(wishId);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wishService.removeById(id);
        return Result.success();
    }
    
    /**
     * 设置心愿预算
     * POST /api/wish/{id}/budget
     */
    @PostMapping("/{id}/budget")
    public Result<Void> setBudget(@PathVariable Long id, @RequestBody WishBudgetDTO dto) {
        wishService.setBudget(id, dto);
        return Result.success();
    }
    
    /**
     * 获取预算统计
     * GET /api/wish/budget-stats
     */
    @GetMapping("/budget-stats")
    public Result<BudgetStatsVO> getBudgetStats(@RequestParam Long familyId) {
        return Result.success(wishService.getBudgetStats(familyId));
    }
}
