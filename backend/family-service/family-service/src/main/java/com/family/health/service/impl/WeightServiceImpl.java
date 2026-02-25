package com.family.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.health.entity.WeightRecord;
import com.family.health.mapper.WeightRecordMapper;
import com.family.health.service.WeightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 体重记录服务实现
 */
@Service
@RequiredArgsConstructor
public class WeightServiceImpl implements WeightService {
    
    private final WeightRecordMapper weightRecordMapper;
    
    @Override
    public WeightRecord saveRecord(WeightRecord record) {
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        weightRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public List<WeightRecord> getHistory(Long userId) {
        LambdaQueryWrapper<WeightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeightRecord::getUserId, userId)
               .orderByDesc(WeightRecord::getRecordDate);
        return weightRecordMapper.selectList(wrapper);
    }
    
    @Override
    public WeightRecord getLatest(Long userId) {
        LambdaQueryWrapper<WeightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeightRecord::getUserId, userId)
               .orderByDesc(WeightRecord::getRecordDate)
               .last("LIMIT 1");
        return weightRecordMapper.selectOne(wrapper);
    }
}
