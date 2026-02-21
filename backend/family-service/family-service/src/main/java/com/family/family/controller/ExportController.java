package com.family.family.controller;

import cn.hutool.json.JSONUtil;
import com.family.common.core.Result;
import com.family.family.entity.*;
import com.family.family.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据导出控制器
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {
    
    private final TaskMapper taskMapper;
    private final WishMapper wishMapper;
    private final FamilyReportMapper reportMapper;
    private final CouponMapper couponMapper;
    private final InventoryMapper inventoryMapper;
    
    public ExportController(TaskMapper taskMapper, WishMapper wishMapper, 
                           FamilyReportMapper reportMapper, CouponMapper couponMapper,
                           InventoryMapper inventoryMapper) {
        this.taskMapper = taskMapper;
        this.wishMapper = wishMapper;
        this.reportMapper = reportMapper;
        this.couponMapper = couponMapper;
        this.inventoryMapper = inventoryMapper;
    }
    
    /**
     * 导出家庭数据
     * GET /api/export/family-data
     */
    @GetMapping("/family-data")
    public Result<Map<String, Object>> exportFamilyData(@RequestParam Long familyId) {
        Map<String, Object> exportData = new HashMap<>();
        
        // 导出任务数据
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
        );
        exportData.put("tasks", tasks);
        
        // 导出心愿数据
        List<Wish> wishes = wishMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Wish>()
                .eq(Wish::getFamilyId, familyId)
        );
        exportData.put("wishes", wishes);
        
        // 导出报告数据
        List<FamilyReport> reports = reportMapper.selectByFamilyId(familyId);
        exportData.put("reports", reports);
        
        // 导出优惠券数据
        List<Coupon> coupons = couponMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getFamilyId, familyId)
        );
        exportData.put("coupons", coupons);
        
        // 导出库存数据
        List<Inventory> inventories = inventoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getFamilyId, familyId)
        );
        exportData.put("inventories", inventories);
        
        Map<String, Object> result = new HashMap<>();
        result.put("familyId", familyId);
        result.put("exportTime", java.time.LocalDateTime.now().toString());
        result.put("data", exportData);
        result.put("json", JSONUtil.toJsonStr(exportData));
        
        return Result.success(result);
    }
}