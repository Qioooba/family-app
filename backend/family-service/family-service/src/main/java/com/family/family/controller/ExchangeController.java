package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.dto.request.ExchangeRequest;
import com.family.family.dto.response.ExchangeCouponResponse;
import com.family.family.dto.response.ExchangeRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 兑换中心控制器
 * 用于积分兑换券的管理
 */
@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {
    
    /**
     * 获取可兑换券列表
     * GET /api/exchange/coupons
     */
    @GetMapping("/coupons")
    public Result<List<ExchangeCouponResponse>> getAvailableCoupons(@RequestParam Long familyId) {
        // TODO: 实现获取可兑换券列表
        return Result.success(null);
    }
    
    /**
     * 获取兑换券详情
     * GET /api/exchange/coupon/{id}
     */
    @GetMapping("/coupon/{id}")
    public Result<ExchangeCouponResponse> getCouponDetail(@PathVariable Long id) {
        // TODO: 实现获取券详情
        return Result.success(null);
    }
    
    /**
     * 兑换券
     * POST /api/exchange/do
     */
    @PostMapping("/do")
    public Result<ExchangeRecordResponse> exchange(@RequestBody ExchangeRequest request) {
        // TODO: 实现兑换逻辑
        return Result.success(null);
    }
    
    /**
     * 获取热门兑换券
     * GET /api/exchange/hot
     */
    @GetMapping("/hot")
    public Result<List<ExchangeCouponResponse>> getHotCoupons() {
        // TODO: 实现获取热门券
        return Result.success(null);
    }
    
    /**
     * 获取兑换排行榜
     * GET /api/exchange/ranking
     */
    @GetMapping("/ranking")
    public Result<List<ExchangeRecordResponse>> getExchangeRanking(@RequestParam(defaultValue = "10") Integer limit) {
        // TODO: 实现获取兑换排行
        return Result.success(null);
    }
}
