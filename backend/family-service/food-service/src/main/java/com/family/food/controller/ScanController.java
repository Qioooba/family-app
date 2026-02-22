
package com.family.food.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.food.dto.request.ScanRequest;
import com.family.food.dto.response.ScanResponse;
import com.family.food.service.ScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 扫码识别控制器
 * 用于扫描商品条形码、二维码和食材图片识别
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/scan")
public class ScanController {
    
    private static final Logger log = LoggerFactory.getLogger(ScanController.class);
    
    private final ScanService scanService;
    
    @Autowired
    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }
    
    /**
     * 扫码识别商品
     * POST /api/scan/product
     */
    @PostMapping("/product")
    public Result<ScanResponse> scanProduct(@RequestBody ScanRequest request) {
        log.info("扫码识别请求: userId={}, familyId={}, scanType={}", 
                request.getUserId(), request.getFamilyId(), request.getScanType());
        
        try {
            ScanResponse response = scanService.scanProduct(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("扫码识别失败", e);
            return Result.error("扫码识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量扫码识别
     * POST /api/scan/batch
     */
    @PostMapping("/batch")
    public Result<List<ScanResponse>> scanBatch(@RequestBody List<ScanRequest> requests) {
        log.info("批量扫码识别请求: 共{}个", requests.size());
        
        try {
            List<ScanResponse> responses = scanService.scanBatch(requests);
            return Result.success(responses);
        } catch (Exception e) {
            log.error("批量扫码识别失败", e);
            return Result.error("批量扫码识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 图片识别食材
     * POST /api/scan/ingredient
     */
    @PostMapping("/ingredient")
    public Result<ScanResponse> scanIngredient(@RequestBody ScanRequest request) {
        log.info("图片识别食材请求: userId={}", request.getUserId());
        
        try {
            ScanResponse response = scanService.scanIngredient(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("图片识别失败", e);
            return Result.error("图片识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 识别历史记录
     * GET /api/scan/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public Result<List<ScanResponse>> getScanHistory(@PathVariable Long userId,
                                                      @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取扫码历史: userId={}, limit={}", userId, limit);
        
        try {
            List<ScanResponse> history = scanService.getScanHistory(userId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取扫码历史失败", e);
            return Result.error("获取扫码历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 扫码添加食材到库存
     * POST /api/scan/add-to-inventory
     */
    @PostMapping("/add-to-inventory")
    public Result<Void> addToInventory(@RequestBody ScanRequest request) {
        log.info("扫码添加到库存: userId={}, familyId={}", request.getUserId(), request.getFamilyId());
        
        try {
            scanService.addToInventory(request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加到库存失败", e);
            return Result.error("添加到库存失败: " + e.getMessage());
        }
    }
    
    /**
     * 从条码库查询商品(直接查询)
     * GET /api/scan/barcode/{barcode}
     */
    @GetMapping("/barcode/{barcode}")
    public Result<ScanResponse> queryBarcode(@PathVariable String barcode) {
        log.info("条码库查询: {}", barcode);
        
        try {
            ScanResponse response = scanService.queryFromBarcodeLibrary(barcode);
            if (response == null) {
                return Result.error("商品不存在");
            }
            return Result.success(response);
        } catch (Exception e) {
            log.error("条码查询失败", e);
            return Result.error("条码查询失败: " + e.getMessage());
        }
    }
}
