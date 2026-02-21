package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.PointsCoupon;
import com.family.family.mapper.PointsCouponMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game/coupon")
public class CouponController {
    
    private final PointsCouponMapper couponMapper;
    
    public CouponController(PointsCouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<PointsCoupon>> getCouponList(@PathVariable Long familyId) {
        return Result.success(couponMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PointsCoupon>()
                .eq(PointsCoupon::getFamilyId, familyId)
                .eq(PointsCoupon::getStatus, 1)
        ));
    }
    
    @PostMapping("/exchange")
    public Result<Boolean> exchangeCoupon(@RequestBody ExchangeRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 实现积分兑换逻辑
        return Result.success(true);
    }
    
    public static class ExchangeRequest {
        private Long couponId;
        
        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
    }
}
