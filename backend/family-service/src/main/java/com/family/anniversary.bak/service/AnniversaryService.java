package com.family.anniversary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.anniversary.entity.Anniversary;

import java.util.List;

public interface AnniversaryService extends IService<Anniversary> {
    /**
     * 根据家庭ID获取纪念日列表（按倒计时排序）
     */
    List<Anniversary> listByFamilyId(Long familyId);
    
    /**
     * 获取即将到期纪念日（带倒计时计算）
     * @param familyId 家庭ID
     * @param days 天数范围
     */
    List<Anniversary> getUpcomingAnniversaries(Long familyId, int days);
    
    /**
     * 获取今日相关的纪念日
     * @param familyId 家庭ID
     */
    List<Anniversary> getTodayAnniversaries(Long familyId);
    
    /**
     * 计算倒计时天数
     * @param anniversary 纪念日
     * @return 倒计时天数（0=今天，正数=还有X天，负数=已过去X天）
     */
    Integer calculateDaysUntil(Anniversary anniversary);
}
