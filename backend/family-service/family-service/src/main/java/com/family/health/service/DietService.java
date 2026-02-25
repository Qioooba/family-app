package com.family.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.DietRecord;
import com.family.health.vo.DailyNutritionVO;
import com.family.health.vo.WeeklyNutritionVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DietService extends IService<DietRecord> {
    
    DietRecord addRecord(DietRecord record);
    
    List<DietRecord> getDayRecords(Long userId, LocalDate date);
    
    Map<String, Object> getDayStatistics(Long userId, LocalDate date);
    
    Map<String, Object> getWeekStatistics(Long userId);
    
    List<DietRecord> recognizeFood(String imageBase64);
    
    /**
     * 获取每日营养分析
     * @param userId 用户ID
     * @param date 日期
     * @return 营养分析
     */
    DailyNutritionVO getDailyNutrition(Long userId, LocalDate date);
    
    /**
     * 获取周营养报告
     * @param userId 用户ID
     * @return 周营养报告
     */
    WeeklyNutritionVO getWeeklyNutrition(Long userId);
}
