package com.family.health.controller;

import com.family.common.core.Result;
import com.family.health.entity.WaterRecord;
import com.family.health.mapper.WaterRecordMapper;
import com.family.health.vo.WaterTodayVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 喝水打卡控制器
 */
@RestController
@RequestMapping("/api/diet/water")
@RequiredArgsConstructor
public class WaterController {
    
    private final WaterRecordMapper waterRecordMapper;
    
    // 默认每日目标饮水量 2000ml
    private static final int DEFAULT_TARGET = 2000;
    
    /**
     * 记录喝水
     * POST /api/diet/water
     */
    @PostMapping
    public Result<WaterRecord> record(@RequestBody WaterRecord record) {
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
     * GET /api/diet/water/today
     */
    @GetMapping("/today")
    public Result<WaterTodayVO> getToday(@RequestParam Long userId) {
        LocalDate today = LocalDate.now();
        
        Integer todayAmount = waterRecordMapper.selectTotalAmountByDate(userId, today);
        List<WaterRecord> records = waterRecordMapper.selectByDate(userId, today);
        
        WaterTodayVO vo = new WaterTodayVO();
        vo.setUserId(userId);
        vo.setDate(today.toString());
        vo.setTodayAmount(todayAmount != null ? todayAmount : 0);
        vo.setTargetAmount(DEFAULT_TARGET);
        
        int percent = vo.getTodayAmount() * 100 / DEFAULT_TARGET;
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
}