package com.family.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.DietRecord;
import com.family.health.mapper.DietRecordMapper;
import com.family.health.service.DietService;
import com.family.health.vo.DailyNutritionVO;
import com.family.health.vo.WeeklyNutritionVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DietServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietService {
    
    // 默认营养目标
    private static final BigDecimal DEFAULT_CALORIES = BigDecimal.valueOf(2000);
    private static final BigDecimal DEFAULT_PROTEIN = BigDecimal.valueOf(60);
    private static final BigDecimal DEFAULT_CARBS = BigDecimal.valueOf(250);
    private static final BigDecimal DEFAULT_FAT = BigDecimal.valueOf(70);
    
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
    public DailyNutritionVO getDailyNutrition(Long userId, LocalDate date) {
        List<DietRecord> records = getDayRecords(userId, date);
        
        DailyNutritionVO vo = new DailyNutritionVO();
        vo.setUserId(userId);
        vo.setDate(date.toString());
        
        // 计算总摄入
        BigDecimal totalCalories = BigDecimal.valueOf(records.stream()
                .mapToInt(DietRecord::getCalories).sum());
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
        BigDecimal totalFiber = records.stream()
                .map(DietRecord::getFiber)
                .filter(f -> f != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        vo.setTotalCalories(totalCalories);
        vo.setTotalProtein(totalProtein);
        vo.setTotalCarbs(totalCarbs);
        vo.setTotalFat(totalFat);
        vo.setTotalFiber(totalFiber);
        
        // 设置目标值
        vo.setTargetCalories(DEFAULT_CALORIES);
        vo.setTargetProtein(DEFAULT_PROTEIN);
        vo.setTargetCarbs(DEFAULT_CARBS);
        vo.setTargetFat(DEFAULT_FAT);
        
        // 计算完成百分比
        vo.setCaloriesPercent(calcPercent(totalCalories, DEFAULT_CALORIES));
        vo.setProteinPercent(calcPercent(totalProtein, DEFAULT_PROTEIN));
        vo.setCarbsPercent(calcPercent(totalCarbs, DEFAULT_CARBS));
        vo.setFatPercent(calcPercent(totalFat, DEFAULT_FAT));
        
        // 餐别分布
        Map<String, List<DietRecord>> mealsMap = records.stream()
                .collect(Collectors.groupingBy(DietRecord::getMealType));
        
        Map<String, DailyNutritionVO.MealNutrition> meals = new HashMap<>();
        mealsMap.forEach((type, mealRecords) -> {
            DailyNutritionVO.MealNutrition meal = new DailyNutritionVO.MealNutrition();
            meal.setMealType(type);
            meal.setCalories(BigDecimal.valueOf(mealRecords.stream()
                    .mapToInt(DietRecord::getCalories).sum()));
            meal.setProtein(mealRecords.stream()
                    .map(DietRecord::getProtein)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            meal.setCarbs(mealRecords.stream()
                    .map(DietRecord::getCarbs)
                    .filter(c -> c != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            meal.setFat(mealRecords.stream()
                    .map(DietRecord::getFat)
                    .filter(f -> f != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            if (totalCalories.compareTo(BigDecimal.ZERO) > 0) {
                meal.setPercent(meal.getCalories().multiply(BigDecimal.valueOf(100))
                        .divide(totalCalories, 0, RoundingMode.HALF_UP).intValue());
            }
            meals.put(type, meal);
        });
        vo.setMeals(meals);
        
        // 生成建议
        vo.setSuggestions(generateSuggestions(totalCalories, totalProtein, totalCarbs, totalFat));
        
        return vo;
    }
    
    @Override
    public WeeklyNutritionVO getWeeklyNutrition(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today;
        
        List<DietRecord> records = lambdaQuery()
                .eq(DietRecord::getUserId, userId)
                .between(DietRecord::getRecordDate, weekStart, weekEnd)
                .list();
        
        WeeklyNutritionVO vo = new WeeklyNutritionVO();
        vo.setUserId(userId);
        vo.setWeekStart(weekStart.toString());
        vo.setWeekEnd(weekEnd.toString());
        
        // 按日期分组
        Map<LocalDate, List<DietRecord>> dailyRecords = records.stream()
                .collect(Collectors.groupingBy(DietRecord::getRecordDate));
        
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(weekStart, weekEnd) + 1;
        
        // 计算总摄入
        BigDecimal totalCalories = BigDecimal.valueOf(records.stream()
                .mapToInt(DietRecord::getCalories).sum());
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
        
        // 计算平均值
        vo.setTotalCalories(totalCalories);
        vo.setTotalProtein(totalProtein);
        vo.setTotalCarbs(totalCarbs);
        vo.setTotalFat(totalFat);
        
        if (days > 0) {
            vo.setAvgCalories(totalCalories.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP));
            vo.setAvgProtein(totalProtein.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP));
            vo.setAvgCarbs(totalCarbs.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP));
            vo.setAvgFat(totalFat.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP));
        }
        
        // 目标完成率
        BigDecimal targetWeeklyCalories = DEFAULT_CALORIES.multiply(BigDecimal.valueOf(days));
        vo.setCaloriesAchievement(calcPercent(totalCalories, targetWeeklyCalories));
        vo.setProteinAchievement(calcPercent(totalProtein, DEFAULT_PROTEIN.multiply(BigDecimal.valueOf(days))));
        
        // 每日摘要
        List<WeeklyNutritionVO.DailySummary> summaries = new ArrayList<>();
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            List<DietRecord> dayRecords = dailyRecords.getOrDefault(date, new ArrayList<>());
            BigDecimal dayCalories = BigDecimal.valueOf(dayRecords.stream()
                    .mapToInt(DietRecord::getCalories).sum());
            BigDecimal dayProtein = dayRecords.stream()
                    .map(DietRecord::getProtein)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            WeeklyNutritionVO.DailySummary summary = new WeeklyNutritionVO.DailySummary();
            summary.setDate(date.toString());
            summary.setCalories(dayCalories);
            summary.setProtein(dayProtein);
            summary.setGoalAchieved(dayCalories.compareTo(DEFAULT_CALORIES.multiply(BigDecimal.valueOf(0.9))) >= 0
                    && dayCalories.compareTo(DEFAULT_CALORIES.multiply(BigDecimal.valueOf(1.1))) <= 0);
            summaries.add(summary);
        }
        vo.setDailySummaries(summaries);
        
        // 健康评分
        int score = calculateHealthScore(vo);
        vo.setHealthScore(score);
        vo.setRecommendations(generateWeeklyRecommendations(vo));
        
        return vo;
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
    
    private Integer calcPercent(BigDecimal actual, BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) == 0) return 0;
        return actual.multiply(BigDecimal.valueOf(100))
                .divide(target, 0, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(999))
                .intValue();
    }
    
    private List<String> generateSuggestions(BigDecimal calories, BigDecimal protein, 
                                              BigDecimal carbs, BigDecimal fat) {
        List<String> suggestions = new ArrayList<>();
        
        if (calories.compareTo(DEFAULT_CALORIES.multiply(BigDecimal.valueOf(0.8))) < 0) {
            suggestions.add("今日摄入热量偏低，建议适当增加主食或蛋白质摄入");
        } else if (calories.compareTo(DEFAULT_CALORIES.multiply(BigDecimal.valueOf(1.2))) > 0) {
            suggestions.add("今日摄入热量偏高，建议控制高热量食物摄入");
        }
        
        if (protein.compareTo(DEFAULT_PROTEIN.multiply(BigDecimal.valueOf(0.8))) < 0) {
            suggestions.add("蛋白质摄入不足，建议增加肉类、蛋类或豆制品");
        }
        
        if (fat.compareTo(DEFAULT_FAT.multiply(BigDecimal.valueOf(1.2))) > 0) {
            suggestions.add("脂肪摄入偏高，建议减少油腻食物");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("今日营养摄入均衡，继续保持！");
        }
        
        return suggestions;
    }
    
    private int calculateHealthScore(WeeklyNutritionVO vo) {
        int score = 80;
        
        // 根据完成率调整分数
        if (vo.getCaloriesAchievement() != null) {
            int diff = Math.abs(vo.getCaloriesAchievement() - 100);
            score -= diff / 10;
        }
        
        // 根据目标达成天数调整
        long achievedDays = vo.getDailySummaries().stream()
                .filter(WeeklyNutritionVO.DailySummary::getGoalAchieved)
                .count();
        score += achievedDays * 2;
        
        return Math.max(0, Math.min(100, score));
    }
    
    private List<String> generateWeeklyRecommendations(WeeklyNutritionVO vo) {
        List<String> recommendations = new ArrayList<>();
        
        if (vo.getAvgCalories() != null && vo.getAvgCalories().compareTo(DEFAULT_CALORIES) < 0) {
            recommendations.add("本周平均热量摄入偏低，建议适当增加饮食量");
        }
        
        if (vo.getCaloriesAchievement() != null && vo.getCaloriesAchievement() < 80) {
            recommendations.add("本周热量目标完成率较低，建议规律进餐");
        }
        
        long goodDays = vo.getDailySummaries().stream()
                .filter(WeeklyNutritionVO.DailySummary::getGoalAchieved)
                .count();
        if (goodDays >= 5) {
            recommendations.add("本周饮食控制良好，继续保持！");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("本周饮食记录完整，建议继续保持均衡饮食");
        }
        
        return recommendations;
    }
}
