package com.family.health.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.health.entity.WaterRecord;
import com.family.health.mapper.WaterRecordMapper;
import com.family.health.service.UserWaterGoalService;
import com.family.health.vo.WaterTodayVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 喝水打卡控制器
 */
@RestController
@RequestMapping("/api/health/water")
public class WaterController {
    
    private final WaterRecordMapper waterRecordMapper;
    private final UserWaterGoalService userWaterGoalService;
    
    // 默认每日目标饮水量 2000ml
    private static final int DEFAULT_TARGET = 2000;
    
    public WaterController(WaterRecordMapper waterRecordMapper, UserWaterGoalService userWaterGoalService) {
        this.waterRecordMapper = waterRecordMapper;
        this.userWaterGoalService = userWaterGoalService;
    }
    
    /**
     * 记录喝水
     * POST /api/health/water
     */
    @PostMapping
    public Result<WaterRecord> record(@RequestBody WaterRecord record) {
        // 从token获取用户ID，如果没有token则使用传入的userId（开发测试用）
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录，使用传入的userId
            userId = record.getUserId();
            if (userId == null) {
                // 使用 4010 而不是 401，避免触发前端全局登出逻辑
                return Result.error(4010, "请先登录");
            }
        }
        record.setUserId(userId);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalTime.now());
        }
        waterRecordMapper.insert(record);
        return Result.success(record);
    }
    
    /**
     * 获取今日喝水量
     * GET /api/health/water/today
     */
    @GetMapping("/today")
    public Result<WaterTodayVO> getToday(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            try {
                userId = StpUtil.getLoginIdAsLong();
            } catch (Exception e) {
                return Result.error(4010, "请先登录");
            }
        }
        LocalDate today = LocalDate.now();
        
        // 获取用户设置的目标饮水量
        Integer targetAmount = userWaterGoalService.getUserTarget(userId);
        
        Integer todayAmount = waterRecordMapper.selectTotalAmountByDate(userId, today);
        List<WaterRecord> records = waterRecordMapper.selectByDate(userId, today);
        
        WaterTodayVO vo = new WaterTodayVO();
        vo.setUserId(userId);
        vo.setDate(today.toString());
        vo.setTodayAmount(todayAmount != null ? todayAmount : 0);
        vo.setTargetAmount(targetAmount);
        
        int percent = targetAmount > 0 ? vo.getTodayAmount() * 100 / targetAmount : 0;
        vo.setPercent(Math.min(percent, 100));
        vo.setRecords(records);
        
        // 计算达成状态
        if (percent >= 100) {
            vo.setStatus("completed");
            vo.setMessage("今日饮水目标已达成，太棒了！");
        } else if (percent >= 50) {
            vo.setStatus("half");
            vo.setMessage("已完成一半，继续加油！");
        } else {
            vo.setStatus("ongoing");
            vo.setMessage("记得多喝水哦~");
        }
        
        return Result.success(vo);
    }
    
    /**
     * 设置饮水目标
     * POST /api/health/water/target
     */
    @PostMapping("/target")
    public Result<Map<String, Object>> setTarget(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        System.out.println("[设置饮水目标] ========== 请求开始 ==========");
        System.out.println("[设置饮水目标] 请求参数: " + params);
        
        // 详细的token调试
        String authHeader = request.getHeader("Authorization");
        System.out.println("[设置饮水目标] Authorization Header: " + authHeader);
        System.out.println("[设置饮水目标] 所有请求头: ");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("  " + headerName + ": " + request.getHeader(headerName));
        }
        
        // 检查Sa-Token状态
        System.out.println("[设置饮水目标] Sa-Token 当前token: " + StpUtil.getTokenValue());
        System.out.println("[设置饮水目标] Sa-Token 是否登录: " + StpUtil.isLogin());
        
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
            System.out.println("[设置饮水目标] 从token获取用户ID: " + userId);
        } catch (Exception e) {
            // 未登录
            System.err.println("[设置饮水目标] 失败: 用户未登录，" + e.getMessage());
            System.err.println("[设置饮水目标] 异常类型: " + e.getClass().getName());
            return Result.error(4010, "请先登录");
        }
        
        Object targetObj = params.get("targetAmount");
        System.out.println("[设置饮水目标] targetAmount原始值: " + targetObj + ", 类型: " + (targetObj != null ? targetObj.getClass() : "null"));
        
        Integer target = null;
        if (targetObj instanceof Integer) {
            target = (Integer) targetObj;
        } else if (targetObj instanceof Number) {
            target = ((Number) targetObj).intValue();
        } else if (targetObj instanceof String) {
            try {
                target = Integer.parseInt((String) targetObj);
            } catch (NumberFormatException e) {
                System.err.println("[设置饮水目标] 参数格式错误: " + targetObj);
                return Result.error(400, "目标饮水量格式错误");
            }
        }
        
        if (target == null || target < 500 || target > 5000) {
            System.err.println("[设置饮水目标] 参数验证失败: target=" + target);
            return Result.error(400, "目标饮水量应在500-5000ml之间");
        }
        
        System.out.println("[设置饮水目标] 用户ID: " + userId + ", 目标: " + target);
        
        boolean success = userWaterGoalService.setUserTarget(userId, target);
        if (!success) {
            System.err.println("[设置饮水目标] 服务层设置失败");
            return Result.error(500, "设置失败，请重试");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("targetAmount", target);
        result.put("success", true);
        System.out.println("[设置饮水目标] 设置成功: " + result);
        return Result.success(result);
    }
    
    /**
     * 获取饮水目标
     * GET /api/health/water/target
     */
    @GetMapping("/target")
    public Result<Map<String, Object>> getTarget(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            try {
                userId = StpUtil.getLoginIdAsLong();
            } catch (Exception e) {
                return Result.error(4010, "请先登录");
            }
        }

        Integer target = userWaterGoalService.getUserTarget(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("targetAmount", target);
        result.put("defaultTarget", DEFAULT_TARGET);
        return Result.success(result);
    }
    
    /**
     * 删除喝水记录
     * DELETE /api/health/water/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        System.out.println("[删除喝水记录] 请求开始，记录ID: " + id);
        
        if (id == null || id <= 0) {
            System.err.println("[删除喝水记录] 失败: 无效的记录ID: " + id);
            return Result.error(400, "无效的记录ID");
        }
        
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
            System.out.println("[删除喝水记录] 当前登录用户ID: " + userId);
        } catch (Exception e) {
            System.err.println("[删除喝水记录] 失败: 用户未登录，无法获取用户ID");
            // 使用 4010 而不是 401，避免触发前端全局登出逻辑
            return Result.error(4010, "请先登录");
        }
        
        // 验证记录是否属于当前用户
        WaterRecord record = waterRecordMapper.selectById(id);
        if (record == null) {
            System.err.println("[删除喝水记录] 失败: 记录不存在，ID: " + id);
            return Result.error(404, "记录不存在");
        }
        
        System.out.println("[删除喝水记录] 查询到记录: " + record);
        System.out.println("[删除喝水记录] 记录用户ID: " + record.getUserId() + ", 当前用户ID: " + userId);
        
        if (!record.getUserId().equals(userId)) {
            System.err.println("[删除喝水记录] 失败: 无权删除，记录用户ID: " + record.getUserId() + ", 当前用户ID: " + userId);
            return Result.error(403, "无权删除此记录");
        }
        
        try {
            int result = waterRecordMapper.deleteById(id);
            System.out.println("[删除喝水记录] 删除成功，影响行数: " + result);
            return Result.success();
        } catch (Exception e) {
            System.err.println("[删除喝水记录] 删除异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定日期的喝水记录
     * GET /api/health/water/history
     */
    @GetMapping("/history")
    public Result<WaterTodayVO> getHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam String date) {
        if (userId == null) {
            try {
                userId = StpUtil.getLoginIdAsLong();
            } catch (Exception e) {
                return Result.error(4010, "请先登录");
            }
        }
        LocalDate queryDate = LocalDate.parse(date);
        
        // 获取用户设置的目标饮水量
        Integer targetAmount = userWaterGoalService.getUserTarget(userId);
        
        Integer todayAmount = waterRecordMapper.selectTotalAmountByDate(userId, queryDate);
        List<WaterRecord> records = waterRecordMapper.selectByDate(userId, queryDate);
        
        WaterTodayVO vo = new WaterTodayVO();
        vo.setUserId(userId);
        vo.setDate(date);
        vo.setTodayAmount(todayAmount != null ? todayAmount : 0);
        vo.setTargetAmount(targetAmount);
        
        int percent = targetAmount > 0 ? vo.getTodayAmount() * 100 / targetAmount : 0;
        vo.setPercent(Math.min(percent, 100));
        vo.setRecords(records);
        
        return Result.success(vo);
    }
}
