package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Schedule;
import com.family.family.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排班控制器
 */
@RestController
@RequestMapping("/api/schedule")
@SaCheckLogin
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 获取排班列表
     * GET /api/schedule/list
     */
    @GetMapping("/list")
    public Result<List<Schedule>> list(
            @RequestParam Long familyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(scheduleService.list(familyId, startDate, endDate, userId));
    }

    /**
     * 创建排班
     * POST /api/schedule/create
     */
    @PostMapping("/create")
    public Result<Schedule> create(@RequestBody Schedule schedule) {
        Long userId = StpUtil.getLoginIdAsLong();
        schedule.setCreatorId(userId);
        return Result.success(scheduleService.create(schedule, userId));
    }

    /**
     * 更新排班
     * POST /api/schedule/update
     */
    @PostMapping("/update")
    public Result<Schedule> update(@RequestBody Schedule schedule) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(scheduleService.update(schedule, userId));
    }

    /**
     * 删除排班
     * POST /api/schedule/delete
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long scheduleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        scheduleService.delete(scheduleId, userId);
        return Result.success();
    }

    /**
     * 获取我的排班
     * GET /api/schedule/my
     */
    @GetMapping("/my")
    public Result<List<Schedule>> mySchedule(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(scheduleService.mySchedule(userId, startDate, endDate));
    }

    /**
     * 交换排班
     * POST /api/schedule/swap
     */
    @PostMapping("/swap")
    public Result<Void> swap(
            @RequestParam Long scheduleId1,
            @RequestParam Long scheduleId2) {
        Long userId = StpUtil.getLoginIdAsLong();
        scheduleService.swap(scheduleId1, scheduleId2, userId);
        return Result.success();
    }
}
