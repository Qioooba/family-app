package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.entity.Coupon;
import com.family.family.mapper.CouponMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/api")
public class CouponController {
    
    private final CouponMapper couponMapper;
    
    public CouponController(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }
    
    /**
     * 添加优惠券
     * POST /api/coupon
     */
    @PostMapping("/coupon")
    public Result<Coupon> create(@RequestBody Coupon coupon) {
        coupon.setStatus(0); // 未使用
        couponMapper.insert(coupon);
        return Result.success(coupon);
    }
    
    /**
     * 获取优惠券列表
     * GET /api/coupons
     */
    @GetMapping("/coupons")
    public Result<List<Coupon>> getList(@RequestParam Long familyId) {
        List<Coupon> coupons = couponMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getFamilyId, familyId)
                .orderByDesc(Coupon::getCreateTime)
        );
        
        // 检查并更新过期状态
        LocalDate today = LocalDate.now();
        for (Coupon coupon : coupons) {
            if (coupon.getStatus() == 0 && coupon.getExpireDate() != null 
                && coupon.getExpireDate().isBefore(today)) {
                coupon.setStatus(2); // 已过期
                couponMapper.updateById(coupon);
            }
        }
        
        return Result.success(coupons);
    }
    
    /**
     * 标记优惠券为已使用
     * PUT /api/coupon/{id}/use
     */
    @PutMapping("/coupon/{id}/use")
    public Result<Void> useCoupon(@PathVariable Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        coupon.setStatus(1); // 已使用
        couponMapper.updateById(coupon);
        return Result.success();
    }
    
    /**
     * 删除优惠券
     * DELETE /api/coupon/{id}
     */
    @DeleteMapping("/coupon/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponMapper.deleteById(id);
        return Result.success();
    }
}