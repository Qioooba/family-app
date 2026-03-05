package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.DietRecord;
import com.family.family.mapper.DietRecordMapper;
import com.family.family.service.DietRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录服务实现
 */
@Service
public class DietRecordServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietRecordService {

    @Override
    public List<DietRecord> getByUserIdAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getUserId, userId)
               .eq(DietRecord::getRecordDate, date)
               .orderByDesc(DietRecord::getCreateTime);
        return list(wrapper);
    }
}
