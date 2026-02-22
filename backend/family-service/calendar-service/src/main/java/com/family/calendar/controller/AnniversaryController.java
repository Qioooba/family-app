package com.family.calendar.controller;

import com.family.common.core.Result;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.service.AnniversaryService;
import com.family.calendar.util.LunarCalendarUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anniversary")
@RequiredArgsConstructor
public class AnniversaryController {
    
    private final AnniversaryService anniversaryService;
    
    @GetMapping("/list/{familyId}")
    public Result<List<Anniversary>> list(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getFamilyAnniversaries(familyId));
    }
    
    @GetMapping("/upcoming/{familyId}")
    public Result<List<Anniversary>> getUpcoming(@PathVariable Long familyId,
                                                    @RequestParam(defaultValue = "30") int days) {
        return Result.success(anniversaryService.getUpcomingAnniversaries(familyId, days));
    }
    
    @PostMapping("/create")
    public Result<Anniversary> create(@RequestBody Anniversary anniversary) {
        return Result.success(anniversaryService.createAnniversary(anniversary));
    }
    
    @GetMapping("/today/{familyId}")
    public Result<List<Anniversary>> getToday(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getTodayCountdown(familyId));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        anniversaryService.removeById(id);
        return Result.success();
    }
    
    /**
     * 农历转公历
     * GET /anniversary/lunar-to-solar
     */
    @GetMapping("/lunar-to-solar")
    public Result<Map<String, String>> lunarToSolar(@RequestParam int lunarMonth, 
                                                    @RequestParam int lunarDay,
                                                    @RequestParam int year) {
        LocalDate solarDate = LunarCalendarUtil.lunarToSolar(lunarMonth, lunarDay, year);
        Map<String, String> result = new HashMap<>();
        if (solarDate != null) {
            result.put("solarDate", solarDate.toString());
            result.put("lunarDate", LunarCalendarUtil.solarToLunar(solarDate));
        }
        return Result.success(result);
    }
    
    /**
     * 公历转农历
     * GET /anniversary/solar-to-lunar
     */
    @GetMapping("/solar-to-lunar")
    public Result<Map<String, String>> solarToLunar(@RequestParam String solarDate) {
        LocalDate date = LocalDate.parse(solarDate);
        String lunar = LunarCalendarUtil.solarToLunar(date);
        String zodiac = LunarCalendarUtil.getZodiac(date.getYear());
        String ganZhi = LunarCalendarUtil.getGanZhi(date.getYear());
        
        Map<String, String> result = new HashMap<>();
        result.put("solarDate", solarDate);
        result.put("lunarDate", lunar);
        result.put("zodiac", zodiac);
        result.put("ganZhi", ganZhi);
        return Result.success(result);
    }
}
