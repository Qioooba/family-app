package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.common.util.LunarCalendarUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 农历转换控制器
 */
@RestController
@RequestMapping("/api/lunar")
@SaCheckLogin
public class LunarCalendarController {
    
    /**
     * 公历转农历
     * GET /api/lunar/convert
     */
    @GetMapping("/convert")
    public Result<LunarCalendarUtil.LunarDate> solarToLunar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(LunarCalendarUtil.solarToLunar(date));
    }
    
    /**
     * 获取今日农历
     * GET /api/lunar/today
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayLunar() {
        LocalDate today = LocalDate.now();
        LunarCalendarUtil.LunarDate lunar = LunarCalendarUtil.solarToLunar(today);
        
        Map<String, Object> result = new HashMap<>();
        result.put("solarDate", today.toString());
        result.put("lunar", lunar);
        result.put("festivals", LunarCalendarUtil.getNextLunarFestival(today));
        
        return Result.success(result);
    }
    
    /**
     * 获取指定日期的农历信息
     * GET /api/lunar/{date}
     */
    @GetMapping("/{date}")
    public Result<Map<String, Object>> getLunarInfo(@PathVariable String date) {
        LocalDate solarDate = LocalDate.parse(date);
        LunarCalendarUtil.LunarDate lunar = LunarCalendarUtil.solarToLunar(solarDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("solarDate", solarDate.toString());
        result.put("lunar", lunar);
        result.put("isToday", solarDate.equals(LocalDate.now()));
        
        return Result.success(result);
    }
    
    /**
     * 获取生肖
     * GET /api/lunar/zodiac/{year}
     */
    @GetMapping("/zodiac/{year}")
    public Result<Map<String, String>> getZodiac(@PathVariable int year) {
        Map<String, String> result = new HashMap<>();
        result.put("year", String.valueOf(year));
        result.put("zodiac", LunarCalendarUtil.getZodiac(year));
        result.put("yearName", LunarCalendarUtil.getLunarYearString(year));
        return Result.success(result);
    }
}
