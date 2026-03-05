package com.family.family.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.DietRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录服务接口
 */
public interface DietRecordService extends IService<DietRecord> {
    
    /**
     * 根据用户ID和日期获取饮食记录
     */
    List<DietRecord> getByUserIdAndDate(Long userId, LocalDate date);
}
