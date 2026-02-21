package com.family.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.health.entity.DietRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DietService extends IService<DietRecord> {
    
    DietRecord addRecord(DietRecord record);
    
    List<DietRecord> getDayRecords(Long userId, LocalDate date);
    
    Map<String, Object> getDayStatistics(Long userId, LocalDate date);
    
    Map<String, Object> getWeekStatistics(Long userId);
    
    List<DietRecord> recognizeFood(String imageBase64);
}
