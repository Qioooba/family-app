package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.UserWaterGoal;
import com.family.family.entity.WaterRecord;
import com.family.family.mapper.UserWaterGoalMapper;
import com.family.family.mapper.WaterRecordMapper;
import com.family.family.service.WaterRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j

@Service
public class WaterRecordServiceImpl implements WaterRecordService {
    
    @Autowired
    private WaterRecordMapper waterRecordMapper;
    
    @Autowired
    private UserWaterGoalMapper userWaterGoalMapper;
    
    @Override
    public WaterRecord addRecord(Long userId, Integer amount) {
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setRecordDate(LocalDate.now());
        record.setRecordTimeFromLocalTime(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        record.setCreateTime(LocalDateTime.now());
        
        waterRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public List<WaterRecord> getRecordsByDate(Long userId, LocalDate date) {
        return waterRecordMapper.selectByUserIdAndDate(userId, date);
    }
    
    @Override
    public Integer getTotalAmountByDate(Long userId, LocalDate date) {
        Integer total = waterRecordMapper.sumAmountByUserIdAndDate(userId, date);
        return total != null ? total : 0;
    }
    
    @Override
    public Map<String, Object> getTodayWaterInfo(Long userId) {
        LocalDate today = LocalDate.now();
        Integer totalAmount = getTotalAmountByDate(userId, today);
        List<WaterRecord> records = getRecordsByDate(userId, today);
        
        // 从数据库获取用户保存的目标，如果没有则返回默认值 2000ml
        int targetAmount = getUserWaterTarget(userId);
        int percent = targetAmount > 0 ? (totalAmount * 100) / targetAmount : 0;
        String status = totalAmount >= targetAmount ? "completed" : "ongoing";
        String message = totalAmount >= targetAmount ? "今日目标已完成！" : "记得多喝水哦~";
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("date", today.toString());
        result.put("todayAmount", totalAmount);
        result.put("targetAmount", targetAmount);
        result.put("percent", percent);
        result.put("status", status);
        result.put("message", message);
        result.put("records", records);
        
        return result;
    }
    
    @Override
    public boolean deleteRecord(Long userId, Long recordId) {
        WaterRecord record = waterRecordMapper.selectById(recordId);
        if (record == null) {
            return false;
        }
        if (userId == null || !userId.equals(record.getUserId())) {
            log.warn("越权删除喝水记录: userId={}, recordId={}, ownerId={}", userId, recordId, record.getUserId());
            return false;
        }
        return waterRecordMapper.deleteById(recordId) > 0;
    }
    
    @Override
    public Integer getUserWaterTarget(Long userId) {
        QueryWrapper<UserWaterGoal> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        UserWaterGoal goal = userWaterGoalMapper.selectOne(wrapper);
        if (goal != null && goal.getTargetAmount() != null) {
            return goal.getTargetAmount();
        }
        return 2000; // 默认值
    }
    
    @Override
    public Integer saveUserWaterTarget(Long userId, Integer targetAmount) {
        QueryWrapper<UserWaterGoal> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        UserWaterGoal goal = userWaterGoalMapper.selectOne(wrapper);
        
        if (goal == null) {
            // 新建记录
            goal = new UserWaterGoal();
            goal.setUserId(userId);
            goal.setTargetAmount(targetAmount);
            goal.setCreateTime(LocalDateTime.now());
            goal.setUpdateTime(LocalDateTime.now());
            userWaterGoalMapper.insert(goal);
        } else {
            // 更新记录
            goal.setTargetAmount(targetAmount);
            goal.setUpdateTime(LocalDateTime.now());
            userWaterGoalMapper.updateById(goal);
        }
        
        return goal.getTargetAmount();
    }
}
