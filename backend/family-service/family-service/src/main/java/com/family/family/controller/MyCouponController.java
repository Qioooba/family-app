package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.dto.request.MyCouponUseRequest;
import com.family.family.dto.response.MyCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 我的优惠券控制器
 * 用于管理用户已兑换的优惠券
 */
@RestController
@RequestMapping("/api/my-coupon")
@RequiredArgsConstructor
public class MyCouponController {
    
    /**
     * 获取我的优惠券列表
     * GET /api/my-coupon/list/{userId}
     */
    @GetMapping("/list/{userId}")
    public Result<List<MyCouponResponse>> getMyCoupons(@PathVariable Long userId) {
        // TODO: 实现获取我的优惠券列表
        return Result.success(null);
    }
    
    /**
     * 按状态获取优惠券
     * GET /api/my-coupon/list/{userId}/{status}
     */
    @GetMapping("/list/{userId}/{status}")
    public Result<List<MyCouponResponse>> getCouponsByStatus(@PathVariable Long userId,
                                                           @PathVariable String status) {
        // TODO: 实现按状态获取优惠券
        return Result.success(null);
    }
    
    /**
     * 获取即将过期的优惠券
     * GET /api/my-coupon/expiring/{userId}
     */
    @GetMapping("/expiring/{userId}")
    public Result<List<MyCouponResponse>> getExpiringCoupons(@PathVariable Long userId) {
        // TODO: 实现获取即将过期优惠券
        return Result.success(null);
    }
    
    /**
     * 获取优惠券详情
     * GET /api/my-coupon/{recordId}
     */
    @GetMapping("/{recordId}")
    public Result<MyCouponResponse> getCouponDetail(@PathVariable Long recordId) {
        // TODO: 实现获取优惠券详情
        return Result.success(null);
    }
    
    /**
     * 使用优惠券
     * POST /api/my-coupon/use
     */
    @PostMapping("/use")
    public Result<Void> useCoupon(@RequestBody MyCouponUseRequest request) {
        // TODO: 实现使用优惠券
        return Result.success(null);
    }
    
    /**
     * 转赠优惠券
     * POST /api/my-coupon/transfer
     */
    @PostMapping("/transfer")
    public Result<Void> transferCoupon(@RequestParam Long recordId,
                                        @RequestParam Long fromUserId,
                                        @RequestParam Long toUserId) {
        // TODO: 实现转赠优惠券
        return Result.success(null);
    }
    
    /**
     * 获取优惠券统计
     * GET /api/my-coupon/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public Result<Object> getCouponStats(@PathVariable Long userId) {
        // TODO: 实现获取优惠券统计
        return Result.success(null);
    }
}
