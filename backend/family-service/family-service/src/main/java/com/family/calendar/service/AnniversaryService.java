package com.family.calendar.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Anniversary;

import java.util.List;

public interface AnniversaryService extends IService<Anniversary> {
    
    List<Anniversary> getFamilyAnniversaries(Long familyId);
    
    List<Anniversary> getUpcomingAnniversaries(Long familyId, int days);
    
    Anniversary createAnniversary(Anniversary anniversary);
    
    List<Anniversary> getTodayCountdown(Long familyId);
}
