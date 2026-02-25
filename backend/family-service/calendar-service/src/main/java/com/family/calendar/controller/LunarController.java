package com.family.calendar.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.calendar.dto.request.LunarRequest;
import com.family.calendar.dto.response.FestivalResponse;
import com.family.calendar.dto.response.LunarResponse;
import com.family.calendar.util.LunarCalendarUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

/**
 * 农历/黄历控制器
 * 用于查询农历日期、节气、黄历等信息
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/calendar/lunar")
@RequiredArgsConstructor
public class LunarController {
    
    // 农历节日映射
    private static final Map<String, String> LUNAR_FESTIVALS = new HashMap<>();
    static {
        LUNAR_FESTIVALS.put("正月初一", "春节");
        LUNAR_FESTIVALS.put("正月十五", "元宵节");
        LUNAR_FESTIVALS.put("五月初五", "端午节");
        LUNAR_FESTIVALS.put("七月初七", "七夕节");
        LUNAR_FESTIVALS.put("七月十五", "中元节");
        LUNAR_FESTIVALS.put("八月十五", "中秋节");
        LUNAR_FESTIVALS.put("九月初九", "重阳节");
        LUNAR_FESTIVALS.put("腊月初八", "腊八节");
        LUNAR_FESTIVALS.put("腊月廿三", "小年");
        LUNAR_FESTIVALS.put("腊月廿四", "小年");
        LUNAR_FESTIVALS.put("腊月三十", "除夕");
    }
    
    // 公历节日
    private static final Map<String, String> SOLAR_FESTIVALS = new HashMap<>();
    static {
        SOLAR_FESTIVALS.put("01-01", "元旦");
        SOLAR_FESTIVALS.put("02-14", "情人节");
        SOLAR_FESTIVALS.put("03-08", "妇女节");
        SOLAR_FESTIVALS.put("03-12", "植树节");
        SOLAR_FESTIVALS.put("04-01", "愚人节");
        SOLAR_FESTIVALS.put("05-01", "劳动节");
        SOLAR_FESTIVALS.put("05-04", "青年节");
        SOLAR_FESTIVALS.put("06-01", "儿童节");
        SOLAR_FESTIVALS.put("07-01", "建党节");
        SOLAR_FESTIVALS.put("08-01", "建军节");
        SOLAR_FESTIVALS.put("09-10", "教师节");
        SOLAR_FESTIVALS.put("10-01", "国庆节");
        SOLAR_FESTIVALS.put("10-24", "程序员节");
        SOLAR_FESTIVALS.put("11-11", "双十一");
        SOLAR_FESTIVALS.put("12-24", "平安夜");
        SOLAR_FESTIVALS.put("12-25", "圣诞节");
    }
    
    /**
     * 公历转农历
     * GET /api/calendar/lunar/convert/{date}
     */
    @GetMapping("/convert/{date}")
    public Result<LunarResponse> solarToLunar(@PathVariable String date) {
        try {
            LocalDate solarDate = LocalDate.parse(date);
            String lunarDate = LunarCalendarUtil.solarToLunar(solarDate);
            String zodiac = LunarCalendarUtil.getZodiac(solarDate.getYear());
            String ganZhi = LunarCalendarUtil.getGanZhi(solarDate.getYear());
            
            LunarResponse response = new LunarResponse();
            response.setSolarDate(date);
            response.setLunarDate(lunarDate);
            response.setZodiac(zodiac);
            response.setGanZhiYear(ganZhi);
            response.setYear(solarDate.getYear());
            response.setMonth(solarDate.getMonthValue());
            response.setDay(solarDate.getDayOfMonth());
            
            // 检查是否是节日
            String festival = LUNAR_FESTIVALS.get(lunarDate);
            response.setFestival(festival);
            
            // 生成宜忌
            response.setYi(generateYiJi(date, true));
            response.setJi(generateYiJi(date, false));
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("日期格式错误，请使用 YYYY-MM-DD 格式");
        }
    }
    
    /**
     * 农历转公历
     * POST /api/calendar/lunar/to-solar
     */
    @PostMapping("/to-solar")
    public Result<LunarResponse> lunarToSolar(@RequestBody LunarRequest request) {
        try {
            LocalDate solarDate = LunarCalendarUtil.lunarToSolar(
                request.getLunarMonth(), 
                request.getLunarDay(), 
                request.getYear()
            );
            
            if (solarDate == null) {
                return Result.fail("无法转换为公历日期");
            }
            
            String lunarDate = LunarCalendarUtil.solarToLunar(solarDate);
            String zodiac = LunarCalendarUtil.getZodiac(solarDate.getYear());
            String ganZhi = LunarCalendarUtil.getGanZhi(solarDate.getYear());
            
            LunarResponse response = new LunarResponse();
            response.setSolarDate(solarDate.toString());
            response.setLunarDate(lunarDate);
            response.setZodiac(zodiac);
            response.setGanZhiYear(ganZhi);
            response.setYear(solarDate.getYear());
            response.setMonth(solarDate.getMonthValue());
            response.setDay(solarDate.getDayOfMonth());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取今日黄历
     * GET /api/calendar/lunar/today
     */
    @GetMapping("/today")
    public Result<LunarResponse> getTodayLunar() {
        LocalDate today = LocalDate.now();
        String dateStr = today.toString();
        return solarToLunar(dateStr);
    }
    
    /**
     * 获取指定日期黄历
     * GET /api/calendar/lunar/almanac/{date}
     */
    @GetMapping("/almanac/{date}")
    public Result<LunarResponse> getAlmanac(@PathVariable String date) {
        return solarToLunar(date);
    }
    
    /**
     * 获取当年节日列表
     * GET /api/calendar/lunar/festivals/{year}
     */
    @GetMapping("/festivals/{year}")
    public Result<FestivalResponse> getFestivals(@PathVariable Integer year) {
        FestivalResponse response = new FestivalResponse();
        response.setYear(year);
        
        List<FestivalResponse.FestivalInfo> festivals = new ArrayList<>();
        
        // 添加农历节日
        for (Map.Entry<String, String> entry : LUNAR_FESTIVALS.entrySet()) {
            String lunarKey = entry.getKey();
            String festivalName = entry.getValue();
            
            String[] parts = lunarKey.split("月");
            if (parts.length == 2) {
                int lunarMonth = parseMonth(parts[0]);
                int lunarDay = parseDay(parts[1]);
                
                LocalDate solarDate = LunarCalendarUtil.lunarToSolar(lunarMonth, lunarDay, year);
                if (solarDate != null) {
                    FestivalResponse.FestivalInfo info = new FestivalResponse.FestivalInfo();
                    info.setName(festivalName);
                    info.setDate(solarDate.toString());
                    info.setMonth(solarDate.getMonthValue());
                    info.setDay(solarDate.getDayOfMonth());
                    info.setType("农历节日");
                    festivals.add(info);
                }
            }
        }
        
        // 添加公历节日
        for (Map.Entry<String, String> entry : SOLAR_FESTIVALS.entrySet()) {
            String dateKey = entry.getKey();
            String festivalName = entry.getValue();
            
            String[] parts = dateKey.split("-");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            
            FestivalResponse.FestivalInfo info = new FestivalResponse.FestivalInfo();
            info.setName(festivalName);
            info.setDate(String.format("%d-%02d-%02d", year, month, day));
            info.setMonth(month);
            info.setDay(day);
            info.setType("公历节日");
            festivals.add(info);
        }
        
        // 按日期排序
        festivals.sort(Comparator.comparing(FestivalResponse.FestivalInfo::getDate));
        
        response.setFestivals(festivals);
        return Result.success(response);
    }
    
    /**
     * 获取当月节日
     * GET /api/calendar/lunar/festivals/{year}/{month}
     */
    @GetMapping("/festivals/{year}/{month}")
    public Result<FestivalResponse> getMonthFestivals(@PathVariable Integer year,
                                                       @PathVariable Integer month) {
        Result<FestivalResponse> fullResult = getFestivals(year);
        if (fullResult.getData() == null) {
            return fullResult;
        }
        
        FestivalResponse response = new FestivalResponse();
        response.setYear(year);
        response.setMonth(month);
        
        List<FestivalResponse.FestivalInfo> monthFestivals = fullResult.getData().getFestivals()
            .stream()
            .filter(f -> f.getMonth() == month)
            .toList();
        
        response.setFestivals(monthFestivals);
        return Result.success(response);
    }
    
    /**
     * 获取近期节日
     * GET /api/calendar/lunar/festivals/upcoming
     */
    @GetMapping("/festivals/upcoming")
    public Result<List<FestivalResponse.FestivalInfo>> getUpcomingFestivals(
            @RequestParam(defaultValue = "30") Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        List<FestivalResponse.FestivalInfo> upcoming = new ArrayList<>();
        int currentYear = today.getYear();
        
        // 获取当年和次年的节日
        for (int year = currentYear; year <= currentYear + 1; year++) {
            Result<FestivalResponse> result = getFestivals(year);
            if (result.getData() != null) {
                for (FestivalResponse.FestivalInfo festival : result.getData().getFestivals()) {
                    LocalDate festivalDate = LocalDate.parse(festival.getDate());
                    if ((festivalDate.isAfter(today) || festivalDate.isEqual(today)) 
                        && festivalDate.isBefore(endDate)) {
                        festival.setDaysUntil((int) java.time.temporal.ChronoUnit.DAYS.between(today, festivalDate));
                        upcoming.add(festival);
                    }
                }
            }
        }
        
        // 按日期排序
        upcoming.sort(Comparator.comparing(FestivalResponse.FestivalInfo::getDate));
        
        return Result.success(upcoming.stream().limit(10).toList());
    }
    
    /**
     * 获取节气信息
     * GET /api/calendar/lunar/solar-terms/{year}
     */
    @GetMapping("/solar-terms/{year}")
    public Result<List<LunarResponse>> getSolarTerms(@PathVariable Integer year) {
        List<LunarResponse> terms = new ArrayList<>();
        
        // 节气日期（简化版，实际需要更精确的计算）
        String[][] solarTerms = {
            {"小寒", "01-05"}, {"大寒", "01-20"}, {"立春", "02-04"}, {"雨水", "02-19"},
            {"惊蛰", "03-05"}, {"春分", "03-20"}, {"清明", "04-05"}, {"谷雨", "04-20"},
            {"立夏", "05-05"}, {"小满", "05-21"}, {"芒种", "06-06"}, {"夏至", "06-21"},
            {"小暑", "07-07"}, {"大暑", "07-23"}, {"立秋", "08-07"}, {"处暑", "08-23"},
            {"白露", "09-07"}, {"秋分", "09-23"}, {"寒露", "10-08"}, {"霜降", "10-23"},
            {"立冬", "11-07"}, {"小雪", "11-22"}, {"大雪", "12-07"}, {"冬至", "12-22"}
        };
        
        for (String[] term : solarTerms) {
            String dateStr = term[1];
            String[] parts = dateStr.split("-");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            
            LunarResponse response = new LunarResponse();
            response.setSolarDate(String.format("%d-%02d-%02d", year, month, day));
            response.setFestival(term[0]);
            response.setMonth(month);
            response.setDay(day);
            terms.add(response);
        }
        
        return Result.success(terms);
    }
    
    // 生成宜忌（简化版，基于日期生成伪随机结果）
    private List<String> generateYiJi(String date, boolean isYi) {
        int hash = Math.abs(date.hashCode());
        List<String> yiList = Arrays.asList(
            "祭祀", "祈福", "求嗣", "开光", "塑绘", "出行", "搬家", "移徙", "入宅", "安床",
            "开市", "交易", "立券", "挂匾", "栽种", "纳畜", "牧养", "经络", "求医", "治病"
        );
        List<String> jiList = Arrays.asList(
            "动土", "破土", "安葬", "行丧", "开仓", "诉讼", "打猎", "栽种", "出货财", "伐木",
            "纳畜", "牧养", "经络", "入宅", "移徙", "安床", "开市", "交易", "立券", "挂匾"
        );
        
        List<String> source = isYi ? yiList : jiList;
        int count = isYi ? 5 : 3;
        
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(source.get((hash + i) % source.size()));
        }
        return result;
    }
    
    private int parseMonth(String monthStr) {
        Map<String, Integer> monthMap = new HashMap<>();
        monthMap.put("正", 1); monthMap.put("二", 2); monthMap.put("三", 3); monthMap.put("四", 4);
        monthMap.put("五", 5); monthMap.put("六", 6); monthMap.put("七", 7); monthMap.put("八", 8);
        monthMap.put("九", 9); monthMap.put("十", 10); monthMap.put("冬", 11); monthMap.put("腊", 12);
        return monthMap.getOrDefault(monthStr, 1);
    }
    
    private int parseDay(String dayStr) {
        Map<String, Integer> dayMap = new HashMap<>();
        dayMap.put("初一", 1); dayMap.put("初二", 2); dayMap.put("初三", 3); dayMap.put("初四", 4);
        dayMap.put("初五", 5); dayMap.put("初六", 6); dayMap.put("初七", 7); dayMap.put("初八", 8);
        dayMap.put("初九", 9); dayMap.put("初十", 10);
        dayMap.put("十一", 11); dayMap.put("十二", 12); dayMap.put("十三", 13); dayMap.put("十四", 14);
        dayMap.put("十五", 15); dayMap.put("十六", 16); dayMap.put("十七", 17); dayMap.put("十八", 18);
        dayMap.put("十九", 19); dayMap.put("二十", 20);
        dayMap.put("廿一", 21); dayMap.put("廿二", 22); dayMap.put("廿三", 23); dayMap.put("廿四", 24);
        dayMap.put("廿五", 25); dayMap.put("廿六", 26); dayMap.put("廿七", 27); dayMap.put("廿八", 28);
        dayMap.put("廿九", 29); dayMap.put("三十", 30);
        return dayMap.getOrDefault(dayStr, 1);
    }
}
