package com.family.food.controller;

import com.family.common.core.Result;
import com.family.food.dto.request.PriceRequest;
import com.family.food.dto.response.PriceResponse;
import com.family.food.dto.response.PriceTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格比较控制器
 * 用于查询商品在不同商店的价格对比
 */
@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController {
    
    /**
     * 查询商品附近价格
     * POST /api/price/compare
     */
    @PostMapping("/compare")
    public Result<List<PriceResponse>> comparePrice(@RequestBody PriceRequest request) {
        // TODO: 实现价格对比逻辑
        return Result.success(null);
    }
    
    /**
     * 获取商品价格趋势
     * GET /api/price/trend/{barcode}
     */
    @GetMapping("/trend/{barcode}")
    public Result<PriceTrendResponse> getPriceTrend(@PathVariable String barcode,
                                                     @RequestParam(defaultValue = "30") Integer days) {
        // TODO: 实现价格趋势查询
        return Result.success(null);
    }
    
    /**
     * 提交价格信息
     * POST /api/price/submit
     */
    @PostMapping("/submit")
    public Result<Void> submitPrice(@RequestBody PriceRequest request) {
        // TODO: 实现价格提交逻辑
        return Result.success(null);
    }
    
    /**
     * 获取低价提醒商品列表
     * GET /api/price/alerts/{userId}
     */
    @GetMapping("/alerts/{userId}")
    public Result<List<PriceResponse>> getPriceAlerts(@PathVariable Long userId) {
        // TODO: 获取用户设置的低价提醒
        return Result.success(null);
    }
    
    /**
     * 设置价格提醒
     * POST /api/price/alert
     */
    @PostMapping("/alert")
    public Result<Void> setPriceAlert(@RequestBody PriceRequest request) {
        // TODO: 设置价格提醒
        return Result.success(null);
    }
}
