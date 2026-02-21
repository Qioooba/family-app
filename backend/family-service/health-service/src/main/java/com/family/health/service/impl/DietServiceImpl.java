package com.family.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.health.entity.DietRecord;
import com.family.health.mapper.DietRecordMapper;
import com.family.health.service.DietService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DietServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietService {
    
    @Override
    public DietRecord addRecord(DietRecord record) {
        save(record);
        return record;
    }
    
    @Override
    public List<DietRecord> getDayRecords(Long userId, LocalDate date) {
        return lambdaQuery()
                .eq(DietRecord::getUserId, userId)
                .eq(DietRecord::getRecordDate, date)
                .orderByAsc(DietRecord::getRecordTime)
                .list();
    }
    
    @Override
    public Map<String, Object> getDayStatistics(Long userId, LocalDate date) {
        List<DietRecord> records = getDayRecords(userId, date);
        
        int totalCalories = records.stream().mapToInt(DietRecord::getCalories).sum();
        BigDecimal totalProtein = records.stream()
                .map(DietRecord::getProtein)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = records.stream()
                .map(DietRecord::getCarbs)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat = records.stream()
                .map(DietRecord::getFat)
                .filter(f -> f != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCalories", totalCalories);
        result.put("totalProtein", totalProtein);
        result.put("totalCarbs", totalCarbs);
        result.put("totalFat", totalFat);
        result.put("recordCount", records.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getWeekStatistics(Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        
        List<DietRecord> records = lambdaQuery()
                .eq(DietRecord::getUserId, userId)
                .between(DietRecord::getRecordDate, start, end)
                .list();
        
        Map<String, Object> result = new HashMap<>();
        result.put("days", 7);
        result.put("totalRecords", records.size());
        result.put("averageCalories", records.isEmpty() ? 0 : 
                records.stream().mapToInt(DietRecord::getCalories).sum() / 7);
        return result;
    }
    
    @Override
    public List<DietRecord> recognizeFood(String imageBase64) {
        // TODO: 调用AI识别食物
        List<DietRecord> list = new ArrayList<>();
        DietRecord r = new DietRecord();
        r.setFoodName("红烧肉");
        r.setCalories(320);
        r.setProtein(BigDecimal.valueOf(15));
        r.setCarbs(BigDecimal.valueOf(8));
        r.setFat(BigDecimal.valueOf(25));
        list.add(r);
        return list;
    }
}
