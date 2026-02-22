
package com.family.food.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.food.dto.request.PriceRequest;
import com.family.food.dto.response.PriceResponse;
import com.family.food.dto.response.PriceTrendResponse;
import com.family.food.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格比较控制器
 * 用于查询商品在不同商店的价格对比
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/price")
public class PriceController {
    
    private static final Logger log = LoggerFactory.getLogger(PriceController.class);
    
    private final PriceService priceService;
    
    @Autowired
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }
    
    /**
     * 查询商品附近价格
     * POST /api/price/compare
     */
    @PostMapping("/compare")
    public Result<List<PriceResponse>> comparePrice(@RequestBody PriceRequest request) {
        log.info("价格对比请求: barcode={}, productName={}", request.getBarcode(), request.getProductName());
        try {
            List<PriceResponse> responses = priceService.comparePrice(request);
            return Result.success(responses);
        } catch (Exception e) {
            log.error("价格对比失败", e);
            return Result.error("价格对比失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取商品价格趋势
     * GET /api/price/trend/{barcode}
     */
    @GetMapping("/trend/{barcode}")
    public Result<PriceTrendResponse> getPriceTrend(@PathVariable String barcode,
                                                     @RequestParam(defaultValue = "30") Integer days) {
        log.info("价格趋势请求: barcode={}, days={}", barcode, days);
        try {
            PriceTrendResponse response = priceService.getPriceTrend(barcode, days);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取价格趋势失败", e);
            return Result.error("获取价格趋势失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交价格信息
     * POST /api/price/submit
     */
    @PostMapping("/submit")
    public Result<Void> submitPrice(@RequestBody PriceRequest request) {
        log.info("提交价格: barcode={}, store={}, price={}", 
                request.getBarcode(), request.getStoreName(), request.getCurrentPrice());
        try {
            priceService.submitPrice(request);
            return Result.success();
        } catch (Exception e) {
            log.error("提交价格失败", e);
            return Result.error("提交价格失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取低价提醒商品列表
     * GET /api/price/alerts/{userId}
     */
    @GetMapping("/alerts/{userId}")
    public Result<List<PriceResponse>> getPriceAlerts(@PathVariable Long userId) {
        log.info("获取价格提醒: userId={}", userId);
        try {
            List<PriceResponse> responses = priceService.getPriceAlerts(userId);
            return Result.success(responses);
        } catch (Exception e) {
            log.error("获取价格提醒失败", e);
            return Result.error("获取价格提醒失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置价格提醒
     * POST /api/price/alert
     */
    @PostMapping("/alert")
    public Result<Void> setPriceAlert(@RequestBody PriceRequest request) {
        log.info("设置价格提醒: barcode={}, targetPrice={}", 
                request.getBarcode(), request.getCurrentPrice());
        try {
            priceService.setPriceAlert(request);
            return Result.success();
        } catch (Exception e) {
            log.error("设置价格提醒失败", e);
            return Result.error("设置价格提醒失败: " + e.getMessage());
        }
    }
}
