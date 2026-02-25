package com.family.calendar.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.Anniversary;
import com.family.calendar.mapper.AnniversaryMapper;
import com.family.calendar.service.AnniversaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnniversaryServiceImpl extends ServiceImpl<AnniversaryMapper, Anniversary> implements AnniversaryService {
    
    @Override
    public List<Anniversary> getFamilyAnniversaries(Long familyId) {
        return lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .orderByAsc(Anniversary::getTargetDate)
                .list();
    }
    
    @Override
    public List<Anniversary> getUpcomingAnniversaries(Long familyId, int days) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(days);
        return lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .between(Anniversary::getTargetDate, now, future)
                .list();
    }
    
    @Override
    public Anniversary createAnniversary(Anniversary anniversary) {
        save(anniversary);
        return anniversary;
    }
    
    @Override
    public List<Anniversary> getTodayCountdown(Long familyId) {
        LocalDate today = LocalDate.now();
        return lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .ge(Anniversary::getTargetDate, today)
                .orderByAsc(Anniversary::getTargetDate)
                .last("LIMIT 1")
                .list();
    }
}
