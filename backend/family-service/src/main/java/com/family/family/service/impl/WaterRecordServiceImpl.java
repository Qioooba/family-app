package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.UserWaterGoal;
import com.family.family.entity.WaterRecord;
import com.family.family.mapper.UserWaterGoalMapper;
import com.family.family.mapper.WaterRecordMapper;
import com.family.family.service.WaterRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WaterRecordServiceImpl implements WaterRecordService {
    
    @Autowired
    private WaterRecordMapper waterRecordMapper;
    
    @Autowired
    private UserWaterGoalMapper userWaterGoalMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 初始化时创建 user_water_goal 表（如果不存在）
     */
    @PostConstruct
    public void initUserWaterGoalTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS user_water_goal (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID', " +
                    "user_id BIGINT NOT NULL COMMENT '用户ID', " +
                    "target_amount INT NOT NULL DEFAULT 2000 COMMENT '每日目标饮水量（毫升）', " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                    "is_deleted TINYINT DEFAULT 0 COMMENT '是否删除(0-否 1-是)', " +
                    "UNIQUE KEY uk_user_id (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户饮水目标设置表'";
            jdbcTemplate.execute(sql);
            System.out.println("user_water_goal 表初始化完成");
        } catch (Exception e) {
            System.out.println("user_water_goal 表初始化: " + e.getMessage());
        }
    }
    
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
    public boolean deleteRecord(Long recordId) {
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
