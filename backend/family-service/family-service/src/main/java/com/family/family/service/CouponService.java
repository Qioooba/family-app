package com.family.family.service;

import com.family.family.entity.Coupon;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CouponService {
    
    Coupon createCoupon(Coupon coupon);
    
    List<Coupon> getCouponList(Long familyId);
    
    List<Coupon> getCouponsByStatus(Long familyId, Integer status);
    
    Coupon useCoupon(Long id);
    
    void deleteCoupon(Long id);
    
    List<Coupon> checkAndUpdateExpiredCoupons(Long familyId);
    
    List<Coupon> getExpiringSoonCoupons(Long familyId, Integer days);
    
    Map<String, Object> getCouponStatistics(Long familyId);
    
    Integer getCouponStatus(Coupon coupon);
}
