
package com.family.calendar.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.calendar.dto.request.CardRequest;
import com.family.calendar.dto.response.CardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡片管理控制器
 * 用于管理纪念日卡片、生日卡片等各类卡片
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {
    
    /**
     * 创建卡片
     * POST /api/card
     */
    @PostMapping
    public Result<CardResponse> create(@RequestBody CardRequest request) {
        // TODO: 实现创建卡片
        return Result.success(null);
    }
    
    /**
     * 更新卡片
     * PUT /api/card/{id}
     */
    @PutMapping("/{id}")
    public Result<CardResponse> update(@PathVariable Long id, @RequestBody CardRequest request) {
        // TODO: 实现更新卡片
        return Result.success(null);
    }
    
    /**
     * 删除卡片
     * DELETE /api/card/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // TODO: 实现删除卡片
        return Result.success(null);
    }
    
    /**
     * 获取卡片详情
     * GET /api/card/{id}
     */
    @GetMapping("/{id}")
    public Result<CardResponse> getById(@PathVariable Long id) {
        // TODO: 实现获取卡片详情
        return Result.success(null);
    }
    
    /**
     * 获取家庭卡片列表
     * GET /api/card/family/{familyId}
     */
    @GetMapping("/family/{familyId}")
    public Result<List<CardResponse>> getFamilyCards(@PathVariable Long familyId) {
        // TODO: 实现获取家庭卡片列表
        return Result.success(null);
    }
    
    /**
     * 获取即将到期的卡片
     * GET /api/card/upcoming/{familyId}
     */
    @GetMapping("/upcoming/{familyId}")
    public Result<List<CardResponse>> getUpcomingCards(@PathVariable Long familyId,
                                                       @RequestParam(defaultValue = "30") Integer days) {
        // TODO: 实现获取即将到期卡片
        return Result.success(null);
    }
    
    /**
     * 按类型获取卡片
     * GET /api/card/type/{cardType}
     */
    @GetMapping("/type/{cardType}")
    public Result<List<CardResponse>> getCardsByType(@PathVariable String cardType,
                                                     @RequestParam Long familyId) {
        // TODO: 实现按类型获取卡片
        return Result.success(null);
    }
    
    /**
     * 获取今日卡片
     * GET /api/card/today/{familyId}
     */
    @GetMapping("/today/{familyId}")
    public Result<List<CardResponse>> getTodayCards(@PathVariable Long familyId) {
        // TODO: 实现获取今日卡片
        return Result.success(null);
    }
    
    /**
     * 发送卡片祝福
     * POST /api/card/{id}/send-blessing
     */
    @PostMapping("/{id}/send-blessing")
    public Result<Void> sendBlessing(@PathVariable Long id, @RequestParam Long userId) {
        // TODO: 实现发送祝福
        return Result.success(null);
    }
}
