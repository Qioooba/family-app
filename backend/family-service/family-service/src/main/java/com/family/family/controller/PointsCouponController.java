package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.PointsCoupon;
import com.family.family.entity.PointsCouponRecord;
import com.family.family.mapper.PointsCouponMapper;
import com.family.family.mapper.PointsCouponRecordMapper;
import com.family.family.mapper.UserPointsMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 积分兑换券控制器
 */
@RestController
@RequestMapping("/api/game")
public class PointsCouponController {
    
    private final PointsCouponMapper couponMapper;
    private final PointsCouponRecordMapper recordMapper;
    private final UserPointsMapper userPointsMapper;
    
    public PointsCouponController(PointsCouponMapper couponMapper, 
                                  PointsCouponRecordMapper recordMapper,
                                  UserPointsMapper userPointsMapper) {
        this.couponMapper = couponMapper;
        this.recordMapper = recordMapper;
        this.userPointsMapper = userPointsMapper;
    }
    
    /**
     * 获取积分兑换券列表
     */
    @GetMapping("/coupon/list/{familyId}")
    public Result<List<PointsCoupon>> getCouponList(@PathVariable Long familyId) {
        return Result.success(couponMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PointsCoupon>()
                .eq(PointsCoupon::getFamilyId, familyId)
                .eq(PointsCoupon::getStatus, 1)
        ));
    }
    
    /**
     * 兑换积分券
     * POST /api/game/coupon/exchange
     */
    @PostMapping("/coupon/exchange")
    public Result<PointsCouponRecord> exchangeCoupon(@RequestBody ExchangeRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 获取券信息
        PointsCoupon coupon = couponMapper.selectById(request.getCouponId());
        if (coupon == null) {
            return Result.error("券不存在");
        }
        
        // 检查用户积分
        // TODO: 实现积分扣减逻辑
        
        // 创建兑换记录
        PointsCouponRecord record = new PointsCouponRecord();
        record.setUserId(userId);
        record.setCouponId(request.getCouponId());
        record.setPointsSpent(coupon.getPointsCost());
        record.setStatus(0);
        record.setExpireDate(LocalDate.now().plusDays(30)); // 默认30天过期
        recordMapper.insert(record);
        
        return Result.success(record);
    }
    
    /**
     * 获取我的券列表
     * GET /api/game/my-coupons
     */
    @GetMapping("/my-coupons")
    public Result<List<PointsCouponRecord>> getMyCoupons() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(recordMapper.selectByUserId(userId));
    }
    
    /**
     * 使用券
     * POST /api/game/coupon/use
     */
    @PostMapping("/coupon/use")
    public Result<Void> useCoupon(@RequestBody UseRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        PointsCouponRecord record = recordMapper.selectById(request.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error("券记录不存在");
        }
        
        if (record.getStatus() == 1) {
            return Result.error("券已使用");
        }
        
        record.setStatus(1);
        recordMapper.updateById(record);
        
        return Result.success();
    }
    
    public static class ExchangeRequest {
        private Long couponId;
        
        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
    }
    
    public static class UseRequest {
        private Long recordId;
        
        public Long getRecordId() { return recordId; }
        public void setRecordId(Long recordId) { this.recordId = recordId; }
    }
}