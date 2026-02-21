package com.family.calendar.controller;

import com.family.common.core.Result;
import com.family.calendar.dto.request.LunarRequest;
import com.family.calendar.dto.response.FestivalResponse;
import com.family.calendar.dto.response.LunarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 农历/黄历控制器
 * 用于查询农历日期、节气、黄历等信息
 */
@RestController
@RequestMapping("/api/lunar")
@RequiredArgsConstructor
public class LunarController {
    
    /**
     * 公历转农历
     * GET /api/lunar/convert/{date}
     */
    @GetMapping("/convert/{date}")
    public Result<LunarResponse> solarToLunar(@PathVariable String date) {
        // TODO: 实现公历转农历
        return Result.success(null);
    }
    
    /**
     * 农历转公历
     * POST /api/lunar/to-solar
     */
    @PostMapping("/to-solar")
    public Result<LunarResponse> lunarToSolar(@RequestBody LunarRequest request) {
        // TODO: 实现农历转公历
        return Result.success(null);
    }
    
    /**
     * 获取今日黄历
     * GET /api/lunar/today
     */
    @GetMapping("/today")
    public Result<LunarResponse> getTodayLunar() {
        // TODO: 获取今日黄历信息
        return Result.success(null);
    }
    
    /**
     * 获取指定日期黄历
     * GET /api/lunar/almanac/{date}
     */
    @GetMapping("/almanac/{date}")
    public Result<LunarResponse> getAlmanac(@PathVariable String date) {
        // TODO: 获取指定日期黄历
        return Result.success(null);
    }
    
    /**
     * 获取当年节日列表
     * GET /api/lunar/festivals/{year}
     */
    @GetMapping("/festivals/{year}")
    public Result<FestivalResponse> getFestivals(@PathVariable Integer year) {
        // TODO: 获取当年所有节日
        return Result.success(null);
    }
    
    /**
     * 获取当月节日
     * GET /api/lunar/festivals/{year}/{month}
     */
    @GetMapping("/festivals/{year}/{month}")
    public Result<FestivalResponse> getMonthFestivals(@PathVariable Integer year,
                                                       @PathVariable Integer month) {
        // TODO: 获取当月节日
        return Result.success(null);
    }
    
    /**
     * 获取近期节日
     * GET /api/lunar/festivals/upcoming
     */
    @GetMapping("/festivals/upcoming")
    public Result<List<FestivalResponse.FestivalInfo>> getUpcomingFestivals(
            @RequestParam(defaultValue = "30") Integer days) {
        // TODO: 获取近期节日
        return Result.success(null);
    }
    
    /**
     * 获取节气信息
     * GET /api/lunar/solar-terms/{year}
     */
    @GetMapping("/solar-terms/{year}")
    public Result<List<LunarResponse>> getSolarTerms(@PathVariable Integer year) {
        // TODO: 获取当年所有节气
        return Result.success(null);
    }
}
