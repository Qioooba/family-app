package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.common.core.exception.BusinessException;
import com.family.family.entity.Coupon;
import com.family.family.mapper.CouponMapper;
import com.family.family.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CouponServiceImpl implements CouponService {
    
    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);
    
    private final CouponMapper couponMapper;
    
    @Autowired
    public CouponServiceImpl(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }
    
    @Override
    public Coupon createCoupon(Coupon coupon) {
        coupon.setStatus(0);
        couponMapper.insert(coupon);
        return coupon;
    }
    
    @Override
    public List<Coupon> getCouponList(Long familyId) {
        List<Coupon> coupons = couponMapper.selectList(
                new QueryWrapper<Coupon>()
                        .eq("family_id", familyId)
                        .eq("deleted", 0)
                        .orderByDesc("create_time")
        );
        
        return checkAndUpdateExpiredCoupons(coupons);
    }
    
    @Override
    public List<Coupon> getCouponsByStatus(Long familyId, Integer status) {
        List<Coupon> coupons = getCouponList(familyId);
        
        List<Coupon> result = new ArrayList<>();
        for (Coupon coupon : coupons) {
            if (coupon.getStatus().equals(status)) {
                result.add(coupon);
            }
        }
        return result;
    }
    
    @Override
    public Coupon useCoupon(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        
        if (coupon.getStatus() == 1) {
            throw new BusinessException("优惠券已被使用");
        }
        
        if (coupon.getStatus() == 2) {
            throw new BusinessException("优惠券已过期");
        }
        
        coupon.setStatus(1);
        couponMapper.updateById(coupon);
        return coupon;
    }
    
    @Override
    public void deleteCoupon(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon != null) {
            couponMapper.deleteById(id);
        }
    }
    
    @Override
    public List<Coupon> checkAndUpdateExpiredCoupons(Long familyId) {
        List<Coupon> coupons = couponMapper.selectList(
                new QueryWrapper<Coupon>()
                        .eq("family_id", familyId)
                        .eq("deleted", 0)
        );
        
        return checkAndUpdateExpiredCoupons(coupons);
    }
    
    private List<Coupon> checkAndUpdateExpiredCoupons(List<Coupon> coupons) {
        LocalDate today = LocalDate.now();
        List<Coupon> updatedCoupons = new ArrayList<>();
        
        for (Coupon coupon : coupons) {
            if (coupon.getStatus() == 0 && coupon.getExpireDate() != null 
                    && coupon.getExpireDate().isBefore(today)) {
                coupon.setStatus(2);
                couponMapper.updateById(coupon);
                updatedCoupons.add(coupon);
            }
        }
        
        if (!updatedCoupons.isEmpty()) {
            log.info("检测到{}张优惠券已过期并更新状态", updatedCoupons.size());
        }
        
        return coupons;
    }
    
    @Override
    public List<Coupon> getExpiringSoonCoupons(Long familyId, Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        
        List<Coupon> coupons = couponMapper.selectList(
                new QueryWrapper<Coupon>()
                        .eq("family_id", familyId)
                        .eq("status", 0)
                        .le("expire_date", deadline)
                        .ge("expire_date", today)
                        .eq("deleted", 0)
                        .orderByAsc("expire_date")
        );
        
        return coupons;
    }
    
    @Override
    public Map<String, Object> getCouponStatistics(Long familyId) {
        List<Coupon> coupons = getCouponList(familyId);
        
        int unused = 0;
        int used = 0;
        int expired = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal savedValue = BigDecimal.ZERO;
        
        Map<String, Integer> platformCount = new HashMap<>();
        
        for (Coupon coupon : coupons) {
            if (coupon.getDiscountAmount() != null) {
                totalValue = totalValue.add(coupon.getDiscountAmount());
            }
            
            switch (coupon.getStatus()) {
                case 0:
                    unused++;
                    break;
                case 1:
                    used++;
                    if (coupon.getDiscountAmount() != null) {
                        savedValue = savedValue.add(coupon.getDiscountAmount());
                    }
                    break;
                case 2:
                    expired++;
                    break;
            }
            
            String platform = coupon.getPlatform();
            if (platform != null) {
                platformCount.put(platform, platformCount.getOrDefault(platform, 0) + 1);
            }
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", coupons.size());
        statistics.put("unusedCount", unused);
        statistics.put("usedCount", used);
        statistics.put("expiredCount", expired);
        statistics.put("totalValue", totalValue);
        statistics.put("savedValue", savedValue);
        statistics.put("potentialSavings", unused > 0 ? totalValue.subtract(savedValue) : BigDecimal.ZERO);
        statistics.put("platformDistribution", platformCount);
        
        return statistics;
    }
    
    @Override
    public Integer getCouponStatus(Coupon coupon) {
        if (coupon.getStatus() == 1) {
            return 1;
        }
        if (coupon.getStatus() == 2 || 
            (coupon.getExpireDate() != null && coupon.getExpireDate().isBefore(LocalDate.now()))) {
            return 2;
        }
        return 0;
    }
}
