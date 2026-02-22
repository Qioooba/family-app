
package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.family.entity.Coupon;
import com.family.family.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api")
public class CouponController {
    
    private static final Logger log = LoggerFactory.getLogger(CouponController.class);
    
    private final CouponService couponService;
    
    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }
    
    /**
     * 添加优惠券
     * POST /api/coupon
     */
    @PostMapping("/coupon")
    public Result<Coupon> create(@RequestBody Coupon coupon) {
        log.info("创建优惠券: familyId={}, title={}", coupon.getFamilyId(), coupon.getTitle());
        try {
            Coupon created = couponService.createCoupon(coupon);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建优惠券失败", e);
            return Result.error("创建优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取优惠券列表
     * GET /api/coupons
     */
    @GetMapping("/coupons")
    public Result<List<Coupon>> getList(@RequestParam Long familyId) {
        log.info("获取优惠券列表: familyId={}", familyId);
        try {
            List<Coupon> coupons = couponService.getCouponList(familyId);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取优惠券列表失败", e);
            return Result.error("获取优惠券列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 按状态获取优惠券
     * GET /api/coupons/status/{status}
     */
    @GetMapping("/coupons/status/{status}")
    public Result<List<Coupon>> getByStatus(@RequestParam Long familyId, @PathVariable Integer status) {
        log.info("按状态获取优惠券: familyId={}, status={}", familyId, status);
        try {
            List<Coupon> coupons = couponService.getCouponsByStatus(familyId, status);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取优惠券失败", e);
            return Result.error("获取优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取即将过期的优惠券
     * GET /api/coupons/expiring
     */
    @GetMapping("/coupons/expiring")
    public Result<List<Coupon>> getExpiringSoon(@RequestParam Long familyId, 
                                                     @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取即将过期优惠券: familyId={}, days={}", familyId, days);
        try {
            List<Coupon> coupons = couponService.getExpiringSoonCoupons(familyId, days);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取即将过期优惠券失败", e);
            return Result.error("获取即将过期优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取优惠券统计
     * GET /api/coupons/statistics
     */
    @GetMapping("/coupons/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam Long familyId) {
        log.info("获取优惠券统计: familyId={}", familyId);
        try {
            Map<String, Object> statistics = couponService.getCouponStatistics(familyId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取优惠券统计失败", e);
            return Result.error("获取优惠券统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记优惠券为已使用
     * PUT /api/coupon/{id}/use
     */
    @PutMapping("/coupon/{id}/use")
    public Result<Coupon> useCoupon(@PathVariable Long id) {
        log.info("使用优惠券: id={}", id);
        try {
            Coupon coupon = couponService.useCoupon(id);
            return Result.success(coupon);
        } catch (Exception e) {
            log.error("使用优惠券失败", e);
            return Result.error("使用优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除优惠券
     * DELETE /api/coupon/{id}
     */
    @DeleteMapping("/coupon/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除优惠券: id={}", id);
        try {
            couponService.deleteCoupon(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除优惠券失败", e);
            return Result.error("删除优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动检查过期优惠券
     * POST /api/coupons/check-expired
     */
    @PostMapping("/coupons/check-expired")
    public Result<List<Coupon>> checkExpired(@RequestParam Long familyId) {
        log.info("手动检查过期优惠券: familyId={}", familyId);
        try {
            List<Coupon> expired = couponService.checkAndUpdateExpiredCoupons(familyId);
            return Result.success(expired);
        } catch (Exception e) {
            log.error("检查过期优惠券失败", e);
            return Result.error("检查过期优惠券失败: " + e.getMessage());
        }
    }
}
