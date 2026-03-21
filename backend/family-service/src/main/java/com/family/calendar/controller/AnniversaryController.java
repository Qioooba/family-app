
package com.family.calendar.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.service.AnniversaryService;
import com.family.calendar.util.LunarCalendarUtil;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.FamilyMemberMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SaCheckLogin
@RequestMapping("/api/calendar/anniversary")
public class AnniversaryController {

    private final AnniversaryService anniversaryService;
    private final FamilyMemberMapper familyMemberMapper;

    public AnniversaryController(AnniversaryService anniversaryService, FamilyMemberMapper familyMemberMapper) {
        this.anniversaryService = anniversaryService;
        this.familyMemberMapper = familyMemberMapper;
    }

    /**
     * 检查用户是否为家庭成员
     */
    private boolean isFamilyMember(Long familyId, Long userId) {
        if (familyId == null || userId == null) return false;
        FamilyMember member = familyMemberMapper.selectOne(
            new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
        );
        return member != null;
    }

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
        Long userId = StpUtil.getLoginIdAsLong();
        if (!isFamilyMember(anniversary.getFamilyId(), userId)) {
            return Result.error(403, "您不是该家庭的成员，无权创建纪念日");
        }
        return Result.success(anniversaryService.createAnniversary(anniversary));
    }

    @PutMapping("/update")
    public Result<Anniversary> update(@RequestBody Anniversary anniversary) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!isFamilyMember(anniversary.getFamilyId(), userId)) {
            return Result.error(403, "您不是该家庭的成员，无权修改纪念日");
        }
        anniversaryService.updateById(anniversary);
        return Result.success(anniversary);
    }

    @GetMapping("/today/{familyId}")
    public Result<List<Anniversary>> getToday(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getTodayCountdown(familyId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 先查询纪念日，获取 familyId
        Anniversary anniversary = anniversaryService.getById(id);
        if (anniversary == null) {
            return Result.error("纪念日不存在");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        if (!isFamilyMember(anniversary.getFamilyId(), userId)) {
            return Result.error(403, "您不是该家庭的成员，无权删除纪念日");
        }
        anniversaryService.removeById(id);
        return Result.success();
    }
    
    /**
     * 获取家庭成员列表
     * GET /api/calendar/anniversary/members/{familyId}
     */
    @GetMapping("/members/{familyId}")
    public Result<List<FamilyMember>> getFamilyMembers(@PathVariable Long familyId) {
        List<FamilyMember> members = familyMemberMapper.selectList(
            new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .orderByAsc(FamilyMember::getJoinTime)
        );
        return Result.success(members);
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
