package com.family.anniversary.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.family.entity.Anniversary;
import com.family.anniversary.service.AnniversaryService;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anniversary")
@SaCheckLogin
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    public AnniversaryController(AnniversaryService anniversaryService) {
        this.anniversaryService = anniversaryService;
    }

    /**
     * 获取纪念日列表
     * @param familyId 家庭ID
     */
    @GetMapping("/list/{familyId}")
    public Result<List<Anniversary>> list(@PathVariable Long familyId) {
        return Result.success(anniversaryService.listByFamilyId(familyId));
    }

    /**
     * 获取即将到期纪念日
     * @param familyId 家庭ID
     * @param days 天数范围，默认30天
     */
    @GetMapping("/upcoming/{familyId}")
    public Result<List<Anniversary>> upcoming(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(anniversaryService.getUpcomingAnniversaries(familyId, days));
    }

    /**
     * 获取今日相关的纪念日
     * @param familyId 家庭ID
     */
    @GetMapping("/today/{familyId}")
    public Result<List<Anniversary>> today(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getTodayAnniversaries(familyId));
    }

    /**
     * 创建纪念日
     */
    @PostMapping("/create")
    public Result<Boolean> create(@RequestBody Anniversary anniversary) {
        Long userId = StpUtil.getLoginIdAsLong();
        anniversary.setUserId(userId);
        return Result.success(anniversaryService.save(anniversary));
    }

    /**
     * 更新纪念日
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Anniversary anniversary) {
        return Result.success(anniversaryService.updateById(anniversary));
    }

    /**
     * 删除纪念日
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(anniversaryService.removeById(id));
    }
}
