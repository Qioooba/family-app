package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.Task;
import com.family.family.entity.DietRecord;
import com.family.family.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * 数据统计服务
 */
@Service
public class StatsService {
    
    private final TaskMapper taskMapper;
    private final DietRecordMapper dietRecordMapper;
    private final FamilyMemberMapper familyMemberMapper;
    
    public StatsService(TaskMapper taskMapper, DietRecordMapper dietRecordMapper, FamilyMemberMapper familyMemberMapper) {
        this.taskMapper = taskMapper;
        this.dietRecordMapper = dietRecordMapper;
        this.familyMemberMapper = familyMemberMapper;
    }
    
    public Map<String, Object> getTodayOverview(Long userId, Long familyId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        Long todayTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(familyId != null, Task::getFamilyId, familyId)
                        .eq(Task::getStatus, 0)
                        .ge(Task::getDueTime, today.atStartOfDay())
                        .le(Task::getDueTime, today.plusDays(1).atStartOfDay())
        );
        result.put("todayTasks", todayTasks);
        
        Integer todayCalories = Optional.ofNullable(
                dietRecordMapper.sumCaloriesByUserAndDate(userId, today)
        ).orElse(0);
        result.put("todayCalories", todayCalories);
        
        LocalDate weekStart = today.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
        Long weeklyCompletedTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(familyId != null, Task::getFamilyId, familyId)
                        .eq(Task::getStatus, 2)
                        .ge(Task::getFinishTime, weekStart.atStartOfDay())
        );
        result.put("weeklyCompletedTasks", weeklyCompletedTasks);
        
        if (familyId != null) {
            Long memberCount = familyMemberMapper.selectCount(
                    new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId)
            );
            result.put("memberCount", memberCount);
        }
        
        result.put("date", today.toString());
        return result;
    }
    
    public Map<String, Object> getPersonalStats(Long userId, String type, String date) {
        Map<String, Object> result = new HashMap<>();
        
        switch (type) {
            case "daily":
                result = getDailyStats(userId, date != null ? LocalDate.parse(date) : LocalDate.now());
                break;
            case "weekly":
                result = getWeeklyStats(userId);
                break;
            case "monthly":
                result = getMonthlyStats(userId, date != null ? date.substring(0, 7) : LocalDate.now().toString().substring(0, 7));
                break;
        }
        
        return result;
    }
    
    private Map<String, Object> getDailyStats(Long userId, LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        
        Integer calories = Optional.ofNullable(
                dietRecordMapper.sumCaloriesByUserAndDate(userId, date)
        ).orElse(0);
        result.put("calories", calories);
        
        Long dietCount = dietRecordMapper.selectCount(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date)
        );
        result.put("dietCount", dietCount);
        
        return result;
    }
    
    private Map<String, Object> getWeeklyStats(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
        
        List<Map<String, Object>> calorieTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            if (date.isAfter(today)) break;
            
            Integer calories = Optional.ofNullable(
                    dietRecordMapper.sumCaloriesByUserAndDate(userId, date)
            ).orElse(0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("calories", calories);
            calorieTrend.add(dayData);
        }
        result.put("calorieTrend", calorieTrend);
        
        int avgCalories = calorieTrend.isEmpty() ? 0 :
                calorieTrend.stream().mapToInt(d -> (Integer) d.get("calories")).sum() / calorieTrend.size();
        result.put("avgCalories", avgCalories);
        
        return result;
    }
    
    private Map<String, Object> getMonthlyStats(Long userId, String month) {
        Map<String, Object> result = new HashMap<>();
        String yearMonth = month + "-01";
        LocalDate monthStart = LocalDate.parse(yearMonth);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        
        Integer totalCalories = Optional.ofNullable(
                dietRecordMapper.sumCaloriesByUserAndDateRange(userId, monthStart, monthEnd)
        ).orElse(0);
        result.put("totalCalories", totalCalories);
        
        int days = monthStart.lengthOfMonth();
        result.put("avgDailyCalories", totalCalories / days);
        
        Long dietDays = dietRecordMapper.countDistinctDatesByUserAndRange(userId, monthStart, monthEnd);
        result.put("dietDays", dietDays);
        
        return result;
    }
    
    public Map<String, Object> getFamilyStats(Long familyId, String type) {
        Map<String, Object> result = new HashMap<>();
        
        Long memberCount = familyMemberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId)
        );
        result.put("memberCount", memberCount);
        
        LocalDate weekStart = LocalDate.now().with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
        Long completedTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getFamilyId, familyId)
                        .eq(Task::getStatus, 2)
                        .ge(Task::getFinishTime, weekStart.atStartOfDay())
        );
        result.put("weeklyCompletedTasks", completedTasks);
        
        return result;
    }
    
    public Map<String, Object> getTaskStats(Long familyId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        Long totalTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getFamilyId, familyId)
                        .ge(Task::getCreateTime, start.atStartOfDay())
                        .le(Task::getCreateTime, end.plusDays(1).atStartOfDay())
        );
        result.put("totalTasks", totalTasks);
        
        Long completedTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getFamilyId, familyId)
                        .eq(Task::getStatus, 2)
                        .ge(Task::getCreateTime, start.atStartOfDay())
                        .le(Task::getCreateTime, end.plusDays(1).atStartOfDay())
        );
        result.put("completedTasks", completedTasks);
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        result.put("completionRate", BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        
        Map<Integer, Long> statusCount = new HashMap<>();
        for (int status = 0; status <= 3; status++) {
            Long count = taskMapper.selectCount(
                    new LambdaQueryWrapper<Task>()
                            .eq(Task::getFamilyId, familyId)
                            .eq(Task::getStatus, status)
            );
            statusCount.put(status, count);
        }
        result.put("statusDistribution", statusCount);
        
        return result;
    }
    
    public Map<String, Object> getDietStats(Long userId, String type) {
        if ("weekly".equals(type)) {
            return getWeeklyStats(userId);
        } else if ("monthly".equals(type)) {
            return getMonthlyStats(userId, LocalDate.now().toString().substring(0, 7));
        }
        return new HashMap<>();
    }
    
    public Map<String, Object> getYearlyStats(Long familyId, int year) {
        Map<String, Object> result = new HashMap<>();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        
        result.put("keywords", Arrays.asList("温馨", "健康", "成长", "陪伴"));
        
        Long totalTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getFamilyId, familyId)
                        .ge(Task::getCreateTime, yearStart.atStartOfDay())
                        .le(Task::getCreateTime, yearEnd.plusDays(1).atStartOfDay())
        );
        result.put("yearlyTasks", totalTasks);
        
        List<Map<String, Object>> badges = new ArrayList<>();
        if (totalTasks >= 100) {
            badges.add(createBadge("任务达人", "完成100个任务", "🏆"));
        }
        if (totalTasks >= 50) {
            badges.add(createBadge("家务能手", "完成50个任务", "🌟"));
        }
        badges.add(createBadge("家庭守护者", "2025年活跃成员", "❤️"));
        result.put("badges", badges);
        
        return result;
    }
    
    private Map<String, Object> createBadge(String name, String desc, String icon) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("name", name);
        badge.put("description", desc);
        badge.put("icon", icon);
        return badge;
    }
}
