package com.family.anniversary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.anniversary.entity.Anniversary;
import com.family.anniversary.mapper.AnniversaryMapper;
import com.family.anniversary.service.AnniversaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnniversaryServiceImpl extends ServiceImpl<AnniversaryMapper, Anniversary> implements AnniversaryService {
    
    @Override
    public List<Anniversary> listByFamilyId(Long familyId) {
        List<Anniversary> list = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .eq(Anniversary::getStatus, 1)
                .orderByAsc(Anniversary::getTargetDate)
                .list();
        
        // 计算倒计时并排序
        list.forEach(this::calculateAndSetDaysUntil);
        return list.stream()
                .sorted(Comparator.comparingInt(a -> Math.abs(a.getDaysUntil())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Anniversary> getUpcomingAnniversaries(Long familyId, int days) {
        // 获取所有纪念日，计算倒计时后筛选
        List<Anniversary> allList = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .eq(Anniversary::getStatus, 1)
                .list();
        
        // 计算倒计时并筛选即将到期的
        return allList.stream()
                .map(this::calculateAndSetDaysUntil)
                .filter(a -> a.getDaysUntil() >= 0 && a.getDaysUntil() <= days)
                .sorted(Comparator.comparingInt(Anniversary::getDaysUntil))
                .limit(3) // 最多返回3个
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Anniversary> getTodayAnniversaries(Long familyId) {
        List<Anniversary> allList = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .eq(Anniversary::getStatus, 1)
                .list();
        
        return allList.stream()
                .map(this::calculateAndSetDaysUntil)
                .filter(a -> a.getDaysUntil() == 0)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer calculateDaysUntil(Anniversary anniversary) {
        if (anniversary == null || anniversary.getTargetDate() == null) {
            return null;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = anniversary.getTargetDate();
        
        // 如果设置了每年重复，计算下一次纪念日
        if (anniversary.getIsRecurring() != null && anniversary.getIsRecurring() == 1) {
            LocalDate nextDate = targetDate.withYear(today.getYear());
            // 如果今年已过，计算明年的
            if (nextDate.isBefore(today)) {
                nextDate = nextDate.plusYears(1);
            }
            anniversary.setNextAnniversaryDate(nextDate);
            return (int) ChronoUnit.DAYS.between(today, nextDate);
        } else {
            // 不重复，直接计算差值
            int days = (int) ChronoUnit.DAYS.between(today, targetDate);
            anniversary.setNextAnniversaryDate(targetDate);
            return days;
        }
    }
    
    /**
     * 计算并设置倒计时天数
     */
    private Anniversary calculateAndSetDaysUntil(Anniversary anniversary) {
        Integer days = calculateDaysUntil(anniversary);
        anniversary.setDaysUntil(days);
        return anniversary;
    }
    
    @Override
    public boolean save(Anniversary entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getIsRecurring() == null) {
            entity.setIsRecurring(1);
        }
        if (entity.getDateType() == null) {
            entity.setDateType("solar");
        }
        return super.save(entity);
    }
}
